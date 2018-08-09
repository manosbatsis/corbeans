package com.github.manosbatsis.corda.spring.beans.util

import net.corda.core.messaging.CordaRPCOps
import org.slf4j.LoggerFactory

/**
 * Eagerly initialised wrapper of a Node RPC connection proxy.
 *
 * @param nodeParams the RPC connection params
 * @property proxy The RPC proxy.
 */
open class SimpleNodeRpcConnection(
        nodeParams: NodeParams): NodeRpcConnection{

    companion object {
        private val logger = LoggerFactory.getLogger(SimpleNodeRpcConnection::class.java)
    }

    override lateinit var proxy: CordaRPCOps

    init {
        createProxy(nodeParams)
    }

}