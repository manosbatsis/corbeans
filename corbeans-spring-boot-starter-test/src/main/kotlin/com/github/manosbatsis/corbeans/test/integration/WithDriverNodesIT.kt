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
import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.getOrThrow
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * Provides Corda (driver) network per [withDriverNodes] call, using the corbeans'
 * config from `application.properties`. You may override the latter with an
 * additional file in your test classpath, i.e. `src/test/resources/application.properties`.
 *
 * Sample:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * @ExtendWith(SpringExtension::class)
 * class MyWithDriverNodesIntegrationTest : WithDriverNodesIT() {
 *
 *      // tell the driver which cordapp packages to load
 *      override fun getCordappPackages(): List<String> = listOf("net.corda.finance")
 *
 *      @Test
 *      fun `Can create services`() {
 *          withDriverNodes {
 *              assertNotNull(this.services)
 *              assertTrue(this.services.keys.isNotEmpty())
 *          }
 *      }
 *
 *      @Test
 *      fun `Can retrieve node identity`() {
 *          withDriverNodes {
 *              assertNotNull(services["partyANodeService"]?.myIdentity)
 *          }
 *      }
 *
 *      @Test
 *      fun `Can retrieve notaries`() {
 *          withDriverNodes {
 *              assertNotNull(services["partyANodeService"]?.notaries())
 *          }
 *      }
 * }
 * ```
 */
abstract class WithDriverNodesIT {

    companion object {
        private val logger = LoggerFactory.getLogger(WithDriverNodesIT::class.java)
    }

    /**
     * Implement to specify cordapp packages to be scanned by the node driver
     * TODO: move to application config?
     */
    abstract fun getCordappPackages(): List<String>

    @Autowired
    lateinit var cordaNodesProperties: CordaNodesProperties

    protected var started = false
    protected var finished = false
    protected var stopped = false


    /**
     * Load node config from spring-boot application
     */
    open fun getNodeParams(): Map<String, NodeParams> {
        return if (this.cordaNodesProperties.nodes.isNotEmpty()) {
            this.cordaNodesProperties.nodes
        } else {
            throw RuntimeException("Could not find node configurations in application properties")
        }
    }

    /**
     * Launch a network, execute the action code, and shut the network down
     */
    open fun withDriverNodes(action: () -> Unit) {
        logger.debug("withDriverNodes: starting network")
        try {
            val startedRpcAddresses = mutableSetOf<String>()
            // start the driver
            driver(DriverParameters(
                    startNodesInProcess = true,
                    extraCordappPackagesToScan = getCordappPackages())) {

                // Configure nodes per application.properties
                getNodeParams().forEach {
                    try {
                        val nodeName = it.key
                        val nodeParams = it.value

                        // Only start a node per unique address
                        if (!startedRpcAddresses.contains(nodeParams.address)) {
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
                    } catch (e: Exception) {
                        logger.error("Failed starting node {}", e)
                    }
                }

                // mark as started
                started = true
                // call any initialization handling code in subclass
                onNetworkInitialized()
                // execure code in context
                action()

            }

        } catch (e: Exception) {
            logger.error("Failed starting nodes {}", e)
            throw e
        } finally {
            stopped = true
        }
        logger.debug("withDriverNodes: stopping network")
    }

    /** Perform any required initializatioon here */
    open fun onNetworkInitialized(){
        // NO-OP
    }
}