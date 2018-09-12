/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
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
package com.github.manosbatsis.corda.webserver.spring

import com.github.manosbatsis.corda.spring.beans.util.NodeParams
import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.lang.RuntimeException

/**
 * Allows adhoc/individual Corda networkContext contexts for yor tests, e.g.:
 * ```
 *
 * @Test
 * fun `Can retreive flows`() {
 *    networkContext{
 *       val flows: List<String> = service.flows()
 *       assertNotNull(flows)
 *    }
 * }
 * ```
 *
 * Uses the application's (auto)configuration from
 * application.properties ro configure the nework
 */
abstract class AbstractCordaNetworkCapableIntegrationTest {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractCordaNetworkCapableIntegrationTest::class.java)
    }

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
     * Launch a network, execute the action and shut it down
     */
    open fun networkContext(action: () -> Unit) {
        try {
            val cordapps = listOf(
                    "net.corda.finance"
            )

            driver(DriverParameters(
                    startNodesInProcess = true,
                    extraCordappPackagesToScan = cordapps)) {

                getNodeParams().forEach {
                    val nodeName = it.key
                    val nodeParams = it.value
                    val user = User(nodeParams.username, nodeParams.password, setOf("ALL"))

                    startNode(
                            providedName = CordaX500Name(nodeName, "Athens", "GR"),
                            rpcUsers = listOf(user),
                            customOverrides = mapOf(
                                    "rpcSettings.address" to nodeParams.address,
                                    "rpcSettings.adminAddress" to nodeParams.adminAddress)).getOrThrow()
                }
                started = true
                action()

            }

        } catch (e: Exception) {

            logger.error("Failed teardownNodess {}", e)
            e.printStackTrace()
            throw e
        } finally {
            stopped = true
        }
    }


}