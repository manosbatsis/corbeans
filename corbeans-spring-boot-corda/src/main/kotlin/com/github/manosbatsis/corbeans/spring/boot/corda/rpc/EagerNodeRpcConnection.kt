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
package com.github.manosbatsis.corbeans.spring.boot.corda.rpc

import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import net.corda.core.messaging.CordaRPCOps
import org.slf4j.LoggerFactory

/**
 * Eagerly initialised wrapper of a Node RPC connection proxy.
 *
 * @param nodeParams the RPC connection params
 * @property proxy The RPC proxy.
 */
open class EagerNodeRpcConnection(
        nodeParams: NodeParams): NodeRpcConnection(nodeParams) {

    companion object {
        private val logger = LoggerFactory.getLogger(EagerNodeRpcConnection::class.java)
    }

    override val proxy: CordaRPCOps = createProxy()
}
