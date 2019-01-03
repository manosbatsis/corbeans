/**
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

import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeParams
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import org.slf4j.LoggerFactory

/**
 * Uses Corda's node driver to either:
 *
 * - Explicitly start/stop a Corda network
 * - Run some code within the context of an implicit ad hoc Corda network
 *
 * This helper class is not threadsafe as concurrent networks would result in port conflicts.
 */
class NodeDriverHelper(val cordaNodesProperties: CordaNodesProperties) {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeDriverHelper::class.java)

    }

    private var state = State.STOPPED

    private lateinit var shutdownHook: Thread;

    /**
     * Starts the Corda network
     */
    fun startNetwork() {
        logger.debug("startNetwork for nodes: {}", cordaNodesProperties.nodes.keys)
        // Start the network asynchronously
        @Suppress("DeferredResultUnused")
        startNetworkAsync()
        // Shutdown network in case of [Ctrl] + [C] or kill -9
        shutdownHook = Thread {stopNetwork()}
        Runtime.getRuntime().addShutdownHook(shutdownHook)
        // Wait for startup to complete
        var elapsed = 0
        while (state != State.RUNNING) {
            Thread.sleep(1000)
            elapsed += 1000
            logger.debug("startNetwork waiting, elapsed: {}", elapsed)
        }
    }


    /**
     * Stops the Corda network
     */
    fun stopNetwork() {
        logger.debug("stopNetwork for nodes: {}", cordaNodesProperties.nodes.keys)
        try {
            state = State.STOPPING
            // give time for a clean shutdown
            val maxWait = 60000//ms
            var elapsed = 0
            while (state != State.STOPPED && elapsed < maxWait) {
                logger.debug("stopNetwork waiting, elapsed: {}", elapsed)
                elapsed += 1000
                Thread.sleep(1000)
            }
            state = State.STOPPED
        } catch (e: Exception) {
            logger.error("stopNetwork failed:", e)
            throw e
        }
        finally {
            // remove the shutdown hook if it exists
            if(::shutdownHook.isInitialized) Runtime.getRuntime().removeShutdownHook(shutdownHook)
        }
    }

    /**
     * Starts and maintains a network running in parallel with tests
     */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun startNetworkAsync() = GlobalScope.async {
        withDriverNodes{
            var elapsed = 0
            while (state == State.RUNNING) {
                // wait for tests to finish
                Thread.sleep(1000)
                logger.debug("startNetworkAsync waiting, elapsed: {}", elapsed)
            }
        }
    }
    /**
     * Launch a network, execute the action code, and shut the network down
     */
    open fun withDriverNodes(action: () -> Unit) {
        logger.debug("withDriverNodes: starting network")
        // Ensure single network instance
        if (state == State.RUNNING) throw IllegalStateException("Corda network is already running");
        try {
            val startedRpcAddresses = mutableSetOf<String>()
            // start the driver
            driver(DriverParameters(
                    startNodesInProcess = true,
                    extraCordappPackagesToScan = cordaNodesProperties.cordapPackages)) {

                val nodeParamsMap = getNodeParams()
                // Configure nodes per application.properties
                nodeParamsMap.forEach {
                    val nodeName = it.key
                    val nodeParams = it.value

                    // Only start a node per unique address,
                    // ignoring "default" overrides
                    if (!startedRpcAddresses.contains(nodeParams.address)
                            && nodeName != NodeParams.NODENAME_DEFAULT ) {
                        logger.debug("withDriverNodes: starting node, params: {}", nodeParams)
                        // note the address as started
                        startedRpcAddresses.add(nodeParams.address!!)

                        val user = User(nodeParams.username!!, nodeParams.password!!, setOf("ALL"))
                        @Suppress("UNUSED_VARIABLE")
                        val handle = startNode(
                                providedName = CordaX500Name(nodeName, "Athens", "GR"),
                                rpcUsers = listOf(user),
                                customOverrides = mapOf(
                                        "rpcSettings.address" to nodeParams.address,
                                        "rpcSettings.adminAddress" to nodeParams.adminAddress)).getOrThrow()
                    }
                    else {
                        logger.debug("withDriverNodes: skipping node: {}", nodeParams)
                    }
                }

                // mark as started
                state = State.RUNNING
                // execute code in context
                action()
                // mark as stopped
                state = State.STOPPING

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

    /**
     * Load node config from spring-boot application
     */
    fun getNodeParams(): Map<String, NodeParams> {
        return if (this.cordaNodesProperties.nodes.isNotEmpty()) {
            this.cordaNodesProperties.nodes
        } else {
            throw RuntimeException("Could not find node configurations in application properties")
        }
    }
}

private enum class State {
    STOPPED, RUNNING, STOPPING
}