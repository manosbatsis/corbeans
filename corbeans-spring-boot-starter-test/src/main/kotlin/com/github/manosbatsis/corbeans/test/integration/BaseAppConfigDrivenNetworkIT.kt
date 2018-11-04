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
import java.lang.RuntimeException

/**
 * Base internal implementation. Allows starting a Corda network, configuring nodes based on `application.properties`
 */
abstract class BaseAppConfigDrivenNetworkIT {

    companion object {
        private val logger = LoggerFactory.getLogger(BaseAppConfigDrivenNetworkIT::class.java)
    }

    /** Implement to specify cordapp packages to be scanned by the node driver */
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
            // start the driver
            driver(DriverParameters(
                    startNodesInProcess = true,
                    extraCordappPackagesToScan = getCordappPackages())) {

                // configure nodes per application.properties
                getNodeParams().forEach {
                    val nodeName = it.key
                    val nodeParams = it.value
                    val user = User(nodeParams.username, nodeParams.password, setOf("ALL"))
                    @Suppress("UNUSED_VARIABLE")
                    val handle = startNode(
                            providedName = CordaX500Name(nodeName, "Athens", "GR"),
                            rpcUsers = listOf(user),
                            customOverrides = mapOf(
                                    "rpcSettings.address" to nodeParams.address,
                                    "rpcSettings.adminAddress" to nodeParams.adminAddress)).getOrThrow()
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