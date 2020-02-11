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

import com.github.manosbatsis.corbeans.corda.common.NodeParams
import com.github.manosbatsis.corbeans.corda.common.NodesProperties
import com.github.manosbatsis.corbeans.corda.common.Util
import kotlinx.coroutines.experimental.CoroutineScope
import kotlinx.coroutines.experimental.Dispatchers
import kotlinx.coroutines.experimental.SupervisorJob
import kotlinx.coroutines.experimental.isActive
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking
import net.corda.core.concurrent.CordaFuture
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.IllegalFlowLogicException
import net.corda.core.identity.CordaX500Name
import net.corda.testing.common.internal.testNetworkParameters
import net.corda.testing.core.DUMMY_NOTARY_NAME
import net.corda.testing.driver.DriverDSL
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.NodeParameters
import net.corda.testing.driver.NotaryHandle
import net.corda.testing.driver.VerifierType.InMemory
import net.corda.testing.driver.driver
import net.corda.testing.node.NotarySpec
import net.corda.testing.node.User
import net.corda.testing.node.internal.TestCordappImpl
import net.corda.testing.node.internal.findCordapp
import org.slf4j.LoggerFactory


/**
 * Uses Corda's node driver to either:
 *
 * - Explicitly start/stop a Corda network
 * - Run some code within the context of an implicit ad hoc Corda network
 *
 * This helper class is not threadsafe as concurrent networks would result in port conflicts.
 */
class NodeDriverHelper(val cordaNodesProperties: NodesProperties = Util.loadProperties()) {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverHelper::class.java)
    }

    private var state = State.STOPPED
    // Create job and scope
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Default + job)
    private val nodeHandles: MutableMap<String, NodeHandle> = mutableMapOf()
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
        try {
            var elapsed = 0
            // Allow graceful stop
            while (state != State.STOPPED && elapsed <= 5000) {
                // wait for tests to finish
                Thread.sleep(1000)
                elapsed += 1000
            }
            if (state != State.STOPPED) {
                logger.debug("stopNetwork called, nodes: {}", cordaNodesProperties.nodes.keys)
                state = State.STOPPING
                this.nodeHandles.forEach { name, nodeHandle ->
                    nodeHandle.ensureClosed()
                }
                this.nodeHandles.clear()
                this.driverNotaryHandles.flatMap { it.nodeHandles.get() }.forEach { nodeHandle ->
                    nodeHandle.ensureClosed()
                }
                this.driverNotaryHandles.clear()
                state = State.STOPPED
                logger.error("stopNetwork stopped")
            } else logger.debug("stopNetwork called but network is already stopped")
        } finally {
            if(!job.isCompleted) job.cancel()
        }
    }

    private fun NodeHandle.ensureClosed() {
        try {
            this.close()
        } catch (e: Throwable) {
            logger.warn("Error closing node ${this.nodeInfo}: ${e.message}")
        }
    }


    /**
     *
     * Call [withDriverNodes] asynchronously and keep it running while state equals RUNNING,
     * i.e. in parallel with tests
     */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun startNetworkAsync() = runBlocking<Unit> {
        logger.debug("startNetworkAsync called")
        scope.launch {
            try {
                withDriverNodes {
                    var elapsed = 0
                    while (isActive && state == State.RUNNING) {
                        // wait for tests to finish
                        Thread.sleep(1000)
                        elapsed += 1000
                        logger.debug("startNetworkAsync waiting, elapsed: {}", elapsed)
                    }
                }
            } catch (e: Throwable) {
                logger.error("Corda network encountered an error, will stop network.", e)
                state = State.ERROR
            } finally {
                stopNetwork()
            }
        }
    }

    /**
     * Launch a network, execute the action code, and shut the network down
     */
    fun withDriverNodes(action: () -> Unit) {
        logger.debug("withDriverNodes called, config: {}", cordaNodesProperties)
        try {
            // Ensure single network instance
            if (state == State.RUNNING) throw IllegalStateException("Corda network is already running")
            if (state == State.STOPPING) throw IllegalStateException("Corda network is still stopping")
            state = State.STARTING
            // start the driver, using with* to avoid CE 4.2 error
            driver(DriverParameters()
                    .withStartNodesInProcess(true)
                    .withCordappsForAllNodes(cordappsForAllNodes())
                    .withNotarySpecs(notarySpecs())
                    .withNotaryCustomOverrides(notaryCustomOverrides())
                    .withNetworkParameters(testNetworkParameters(minimumPlatformVersion = 4))
            ) {
                nodeHandles.putAll(startNodes())// Configure nodes per application.properties
                driverNotaryHandles.addAll(this.notaryHandles.map {
                    logger.debug("withDriverNodes, adding notary handle: $it")
                    it
                })
                state = State.RUNNING // mark as started
                action() // execute code in context
            }
            state = State.STOPPED // mark as started

        } catch (e: Throwable) {
            // mark as stopped
            state = State.ERROR
            logger.error("Error running nodes {}", e)
            throw e
        }
    }


    private fun DriverDSL.startNodes(): Map<String, NodeHandle> {
        return startNodeFutures().mapValues {
            val handle = it.value.get()
            logger.debug("startNodes started node ${it.key}")
            handle
        }
    }

    private fun DriverDSL.startNodeFutures(): Map<String, CordaFuture<NodeHandle>> {
        // Note addresses to filter out any dupes
        val startedRpcAddresses = mutableSetOf<String>()
        logger.debug("startNodeFutures: starting node, cordaNodesProperties: {}", cordaNodesProperties)
        return getNodeParams().mapNotNull {
            val nodeName = it.key
            val nodeParams = it.value
            val testPartyName = nodeParams.testPartyName
            val x500Name = if (testPartyName != null) CordaX500Name.parse(testPartyName)
            else CordaX500Name(nodeName, "Athens", "GR")

            // Only start a node per unique address,
            // ignoring "default" overrides
            if (!startedRpcAddresses.contains(nodeParams.address)
                    && nodeName != NodeParams.NODENAME_DEFAULT) {

                logger.debug("startNodeFutures: starting node, params: {}", nodeParams)
                // note the address as started
                startedRpcAddresses.add(nodeParams.address!!)

                val user = User(nodeParams.username!!, nodeParams.password!!, setOf("ALL"))
                @Suppress("UNUSED_VARIABLE")
                nodeName to startNode(
                        defaultParameters = NodeParameters(
                                flowOverrides = getFlowOverrides(),
                                rpcUsers = listOf(user),
                                verifierType = InMemory),
                        providedName = x500Name,
                        //rpcUsers = listOf(user),
                        customOverrides = mapOf(
                                "rpcSettings.address" to nodeParams.address,
                                "rpcSettings.adminAddress" to nodeParams.adminAddress))
            } else {
                logger.debug("startNodeFutures: skipping node: {}", nodeParams)
                null
            }
        }.toMap()
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

    private fun notaryCustomOverrides(): Map<String, Any?>  =
        if(cordaNodesProperties.notarySpec.address != null)
            mapOf( "rpcSettings.address" to cordaNodesProperties.notarySpec.address)
        else emptyMap()

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
    STOPPED, STARTING, RUNNING, STOPPING, ERROR
}
