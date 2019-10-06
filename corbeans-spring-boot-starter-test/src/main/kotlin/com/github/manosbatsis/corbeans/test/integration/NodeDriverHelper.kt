/*
 *     Corbeans: Corda integration for Spring Boot
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 3 of the License, or (at your option) any later version.
 *
 *     This library is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *     Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public
 *     License along with this library; if not, write to the Free Software
 *     Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
 *     USA
 */
package com.github.manosbatsis.corbeans.test.integration

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesPropertiesWrapper
import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.SupervisorJob
import kotlinx.coroutines.experimental.launch
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.IllegalFlowLogicException
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.driver.*
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import net.corda.testing.node.internal.TestCordappImpl
import net.corda.testing.node.internal.findCordapp
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Uses Corda's node driver to either:
 *
 * - Explicitly start/stop a Corda network
 * - Run some code within the context of an implicit ad hoc Corda network
 *
 * This helper class is not threadsafe as concurrent networks would result in port conflicts.
 */
class NodeDriverHelper(val cordaNodesProperties: CordaNodesProperties = loadProperties()) {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverHelper::class.java)
        fun loadProperties(): CordaNodesProperties {
            logger.debug("No CordaNodesProperties given, loading from classpath")
            val inputStream = this::class.java.getResourceAsStream("/application.properties")
            val properties = Properties()
            properties.load(inputStream)
            val mapper = JavaPropsMapper()
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            val cordaNodesProperties = mapper
                    .readPropertiesAs(properties, CordaNodesPropertiesWrapper::class.java).corbeans!!
            // Fix parsing
            if (cordaNodesProperties.cordapPackages.size == 1) {
                cordaNodesProperties.cordapPackages = cordaNodesProperties.cordapPackages.first()
                        .replace(',', ' ').split(' ')
            }
            logger.debug("Loaded CordaNodesProperties: $cordaNodesProperties")
            return cordaNodesProperties
        }
    }

    private var state = State.STOPPED
    // Create job and scope
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)

    private lateinit var shutdownHook: Thread;

    private val nodeHandles = mutableMapOf<String, NodeHandle>()
    private val driverNotaryHandles = mutableListOf<NotaryHandle>()

    /**
     * Starts the Corda network
     */
    fun startNetwork() {
        if (state != State.STOPPED) throw IllegalStateException("Corda network is already running")
        state = State.STARTING
        // Start the network asynchronously
        @Suppress("DeferredResultUnused")
        startNetworkAsync()
        // Shutdown network in case of [Ctrl] + [C] or kill -9
        shutdownHook = Thread {stopNetwork()}
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        // Wait for startup to complete
        var elapsed = 0
        while (state == State.STARTING) {
            Thread.sleep(1000)
            elapsed += 1000
        }
        logger.debug("startNetwork started")
    }


    /**
     * Stops the Corda network
     */
    fun stopNetwork() {
        if(state != State.STOPPED) {
            logger.debug("stopNetwork called, nodes: {}", cordaNodesProperties.nodes.keys)
            state = State.STOPPING
            this.nodeHandles.forEach { name, nodeHandle ->
                stopAndClose(nodeHandle)
            }
            this.driverNotaryHandles.flatMap { it.nodeHandles.get() }.forEach { nodeHandle ->
                stopAndClose(nodeHandle)
            }
            state = State.STOPPED
            logger.error("stopNetwork stopped")
            // remove the shutdown hook if it exists
            try {
                if (::shutdownHook.isInitialized) Runtime.getRuntime().removeShutdownHook(shutdownHook)
            } catch (e: Exception) {
                // NO-OP
            }
        }
        else logger.debug("stopNetwork called but network is already stopped")
    }

    private fun stopAndClose(nodeHandle: NodeHandle) {
        val name = nodeHandle.nodeInfo.legalIdentities.first().name
        try {
            nodeHandle.stop()
        } catch (e: Exception) {
            logger.error("Error stopping node $name, ${e.message}")
        }
        finally {
            try{
                nodeHandle.close()
            }catch (e: Exception){
                logger.error("Error closing node $name, ${e.message}")
            }

        }
    }


    /**
     *
     * Call [withDriverNodes] asynchronously and keep it running while state equals RUNNING,
     * i.e. in parallel with tests
     */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun startNetworkAsync() =
        try {
            logger.debug("startNetworkAsync called")
            scope.launch {
                withDriverNodes {
                    var elapsed = 0
                    while (state == State.RUNNING) {
                        // wait for tests to finish
                        Thread.sleep(1000)
                        elapsed += 1000
                        logger.debug("startNetworkAsync waiting, elapsed: {}", elapsed)
                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Corda network encountered an error, will stop network.", e)
            stopNetwork()
        }

    /**
     * May throw Exception

    suspend fun asyncWithDriverNodes() = coroutineScope {
        async {

        }.await()
    }
     */
    /**
     * Launch a network, execute the action code, and shut the network down
     */
    fun withDriverNodes(action: () -> Unit) {
        System.out.println("withDriverNodes called, config: " + cordaNodesProperties)
        logger.debug("withDriverNodes called, config: {}", cordaNodesProperties)
        // Ensure single network instance
        if (state == State.RUNNING) throw IllegalStateException("Corda network is already running")
        if (state == State.STOPPING) throw IllegalStateException("Corda network is still stopping")
        state = State.STARTING
        try {
            val startedRpcAddresses = mutableSetOf<String>()
            // start the driver, using with* to avoid CE 4.2 error
            driver(DriverParameters()
                    .withStartNodesInProcess(true)
                    .withCordappsForAllNodes(cordappsForAllNodes())
                    .withNotarySpecs(notarySpecs())
                    .withNetworkParameters(testNetworkParameters(minimumPlatformVersion = 4))
            ) {
                startNodes(startedRpcAddresses)// Configure nodes per application.properties
                state = State.RUNNING // mark as started
                driverNotaryHandles.addAll(this.notaryHandles)
                logger.debug("withDriverNodes, driverNotaryHandles: $driverNotaryHandles")
                action() // execute code in context
                state = State.STOPPING // mark as stopped
            }
            // mark as stopped
            state = State.STOPPED

        } catch (e: Exception) {
            // mark as stopped
            state = State.STOPPED
            logger.error("Error running nodes {}", e)
            throw e
        }
        logger.debug("withDriverNodes: stopping network")
    }

    private fun DriverDSL.startNodes(startedRpcAddresses: MutableSet<String>) {

        getNodeParams().forEach {
            val nodeName = it.key
            val nodeParams = it.value
            val testPartyName = nodeParams.testPartyName
            val x500Name = if(testPartyName != null) CordaX500Name.parse(testPartyName)
                else CordaX500Name(nodeName, "Athens", "GR")

            // Only start a node per unique address,
            // ignoring "default" overrides
            if (!startedRpcAddresses.contains(nodeParams.address)
                    && nodeName != NodeParams.NODENAME_DEFAULT) {

                logger.debug("withDriverNodes: starting node, cordaNodesProperties: {}", cordaNodesProperties)
                logger.debug("withDriverNodes: starting node, params: {}", nodeParams)
                // note the address as started
                startedRpcAddresses.add(nodeParams.address!!)

                val user = User(nodeParams.username!!, nodeParams.password!!, setOf("ALL"))
                @Suppress("UNUSED_VARIABLE")
                nodeHandles[nodeName] = startNode(
                        defaultParameters = NodeParameters(flowOverrides = getFlowOverrides()),
                        providedName = x500Name,
                        rpcUsers = listOf(user),
                        customOverrides = mapOf(
                                "rpcSettings.address" to nodeParams.address,
                                "rpcSettings.adminAddress" to nodeParams.adminAddress)).getOrThrow()

                logger.debug("withDriverNodes: started node, params: {}", nodeParams)
            } else {
                logger.debug("withDriverNodes: skipping node: {}", nodeParams)
            }
        }
    }

    private fun cordappsForAllNodes(): List<TestCordappImpl> {
        val scanPackages = mutableSetOf<String>()
        val cordappsForAllNodes = cordaNodesProperties.cordapPackages
                .filter { it.isNotBlank() }
                .mapNotNull {
                    logger.debug("Adding cordapp to all driver nodes: {}", it)
                    val cordapp = findCordapp(it)
                    // skip if dupe
                    if (scanPackages.contains(cordapp.scanPackage)) null
                    else {
                        scanPackages.add(cordapp.scanPackage)
                        cordapp
                    }
                }
        return cordappsForAllNodes
    }

    private fun notarySpecs(): List<NotarySpec> {
        val notarySpecs = listOf(NotarySpec(
                name = DUMMY_NOTARY_NAME,
                validating = !cordaNodesProperties.notarySpec.nonValidating))
        return notarySpecs
    }

    /**
     * Load node config from spring-boot application
     */
    fun getNodeParams(): Map<String, NodeParams> {
        logger.debug("getNodeParams called")
        return if (this.cordaNodesProperties.nodes.isNotEmpty()) {
            this.cordaNodesProperties.nodes
        } else {
            throw RuntimeException("Could not find node configurations in application properties")
        }
    }

    fun getFlowOverrides(): Map<out Class<out FlowLogic<*>>, out Class<out FlowLogic<*>>> {
        return this.cordaNodesProperties.flowOverrides.flatMap {
            it.replace(',', ' ').split(' ')
                    .filter { it.isNotBlank() }
        }
                .map { validatedFlowClassFromName(it.trim()) }
                .chunked(2)
                .associate { (a, b) -> a to b }
    }

    private fun validatedFlowClassFromName(flowClassName: String): Class<out FlowLogic<*>> {
        logger.debug("validatedFlowClassFromName: '${flowClassName}'")
        val forName = try {
            Class.forName(flowClassName, true, NodeDriverHelper::class.java.classLoader)
        } catch (e: ClassNotFoundException) {
            throw IllegalFlowLogicException(flowClassName, "Flow not found: $flowClassName")
        }
        return forName.asSubclass(FlowLogic::class.java)
                ?: throw IllegalFlowLogicException(flowClassName, "The class $flowClassName is not a subclass of FlowLogic.")
    }
}

private enum class State {
    STOPPED, STARTING, RUNNING, STOPPING
}
