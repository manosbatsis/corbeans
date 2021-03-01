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

import com.github.manosbatsis.corda.testacles.containers.cordform.CordformNetworkContainer
import org.springframework.test.context.DynamicPropertyRegistry

object TestaclesContainerHelper {

    /** Apply nodes' container info to application properties */
    fun updateRegistry(
        registry: DynamicPropertyRegistry,
        cordformNetworkContainer: CordformNetworkContainer
    ) {
        cordformNetworkContainer.nodes
            .filterNot { (nodeName, instance) ->
                nodeName.toLowerCase().contains("notary")
                        || instance.config.hasPath("notary")
            }
            .forEach { (nodeName, container) ->
                val nodeConf = container.simpleNodeConfig
                val user = container.getDefaultRpcUser()
                val prefix = "corbeans.nodes.$nodeName"
                with(registry) {
                    add("$prefix.partyName") { "${nodeConf.myLegalName}" }
                    add("$prefix.username") { user.username }
                    add("$prefix.password") { user.password }
                    add("$prefix.address") { container.rpcAddress }
                    add("$prefix.adminAddress") { container.rpcAddress }
                    add("$prefix.admin-address") { container.rpcAddress }
                }
            }
    }
}