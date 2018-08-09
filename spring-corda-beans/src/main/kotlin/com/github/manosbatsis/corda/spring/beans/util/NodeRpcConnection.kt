package com.github.manosbatsis.corda.spring.beans.util

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