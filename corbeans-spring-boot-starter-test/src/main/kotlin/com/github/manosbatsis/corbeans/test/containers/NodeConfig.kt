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
package com.github.manosbatsis.corbeans.test.containers

import net.corda.core.identity.CordaX500Name
import net.corda.core.utilities.NetworkHostAndPort
import net.corda.node.services.config.NodeRpcSettings
import net.corda.nodeapi.internal.config.User
import java.util.Properties

data class NodeConfig(
        val myLegalName: CordaX500Name,
        val p2pAddress: NetworkHostAndPort,
        val rpcSettings: NodeRpcSettings,
        /** This is not used by the node but by the webserver which looks at node.conf. */
        val webAddress: NetworkHostAndPort? = null,
        val notary: NotaryService? = null,
        val h2port: Int? = null,
        val rpcUsers: List<User> = listOf(),
        /** Pass-through for generating node.conf with external DB */
        val dataSourceProperties: Properties? = null,
        val database: Properties? = null,
        val systemProperties: Map<String, Any?> = emptyMap(),
        val devMode: Boolean = true,
        val detectPublicIp: Boolean = false,
        val useTestClock: Boolean = true
)
data class NotaryService(val validating: Boolean)  {
        override fun toString(): String = "${if (validating) "V" else "Non-v"}alidating Notary"
}