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
package com.github.manosbatsis.corbeans.spring.boot.corda.util

import net.corda.client.rpc.CordaRPCClient
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit


/**
 * Wraps a Corda Node RPC connection proxy
 */
interface NodeRpcConnection {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeRpcConnection::class.java)
    }

    val proxy: CordaRPCOps

    fun createProxy(nodeParams: NodeParams): CordaRPCOps {
        var created: CordaRPCOps? = null
        var i = 0;
        while (created == null) {
            i++
            try {
                val addressParts = nodeParams.address.split(":")
                val rpcAddress = NetworkHostAndPort(addressParts[0], addressParts[1].toInt())
                val rpcClient = CordaRPCClient(rpcAddress)
                created = rpcClient.start(nodeParams.username, nodeParams.password).proxy
            } catch (e: Exception) {
                logger.error("Failed initializing RPC connection to ${nodeParams.address}", e)
                e.printStackTrace()
                if (i < 0)//nodeParams.retries)
                    TimeUnit.SECONDS.sleep(nodeParams.retryDelaySeconds);
                else {
                    throw RuntimeException(e)
                }
            }
        }
        logger.debug(
                "Initialized RPC connection for ${nodeParams.address} on port ${nodeParams.adminAddress}, name: {}",
                created.nodeInfo().legalIdentities.first().name)
        return created
    }

}