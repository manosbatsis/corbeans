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

import com.github.manosbatsis.corbeans.corda.common.NodeParams
import com.github.manosbatsis.vaultaire.rpc.NodeRpcConnection
import net.corda.client.rpc.*
import net.corda.client.rpc.CordaRPCClientConfiguration.Companion.DEFAULT
import net.corda.core.messaging.ClientRpcSslOptions
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import org.apache.activemq.artemis.api.core.ActiveMQSecurityException
import org.slf4j.LoggerFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.annotation.PreDestroy


/**
 * Wraps a Corda Node RPC connection
 */
abstract class AbstractNodeRpcConnection(
        private val nodeParams: NodeParams
) : NodeRpcConnection {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractNodeRpcConnection::class.java)
    }

    private lateinit var rpcConnection: CordaRPCConnection

    /**
     * Attempt to obtain a [CordaRPCConnection], retry five times with
     * a five second delay in case of an [RPCException]error
     */
    fun createProxy(): CordaRPCOps {
        var created: CordaRPCOps? = null
        var attemptCount = 0
        var attemptInterval = DEFAULT.connectionRetryInterval.toMillis()
        while (created == null) {
            logger.debug("Initializing RPC connection for address {}, attempt: {}", nodeParams.address, attemptCount)
            attemptCount++
            try {
                val gracefulReconnect = GracefulReconnect(
                        onDisconnect= { this.onDisconnect() },
                        onReconnect = { this.onReconnect() },
                        maxAttempts = nodeParams.maxReconnectAttempts ?: 5)
                val rpcClient = CordaRPCClient(
                        hostAndPort = buildRpcAddress(),
                        configuration = buildRpcClientConfig(),
                        sslConfiguration = clientRpcSslOptions()/*,
                        classLoader = AbstractNodeRpcConnection::class.java.classLoader,
                        customSerializers = nodeParams.customSerializers*/  )

                rpcConnection = rpcClient.start(
                        username = nodeParams.username!!,
                        password = nodeParams.password!!,
                        gracefulReconnect = if(nodeParams.disableGracefulReconnect!!) null else gracefulReconnect)
                created = rpcConnection.proxy
            } catch (secEx: ActiveMQSecurityException) {
                // Happens when incorrect credentials provided - no point to retry connecting.
                throw secEx
            } catch (e: RPCException) {
                if (attemptCount >= DEFAULT.maxReconnectAttempts) throw e
                logger.warn("Failed initializing RPC to ${nodeParams.address}, will retry. Error message: ${e.message}", e)
                TimeUnit.MILLISECONDS.sleep(attemptInterval)
                attemptInterval *= DEFAULT.connectionRetryIntervalMultiplier.toLong()
            }
        }
        logger.debug(
                "Initialized RPC connection for ${nodeParams.address} on port ${nodeParams.adminAddress}, name: {}",
                created.nodeInfo().legalIdentities.first().name)
        return created
    }

    /** Override to handle RPC reconnect */
    open fun onReconnect() {
        logger.warn("RPC reconnected")
    }

    /** Override to handle RPC disconnect */
    open fun onDisconnect() {
        logger.warn("RPC disconnected")
    }

    /**
     * Get the [ClientRpcSslOptions] if properly configured
     * @throws [IllegalArgumentException] if the configuration is incomplete
     * @return the options if properly configured, `null` otherwise
     */
    private fun clientRpcSslOptions(): ClientRpcSslOptions? {
        val trustStoreProps = listOf(nodeParams.trustStorePassword, nodeParams.trustStorePath)
        // Check for incomplete SSL configuration
        return if (trustStoreProps.any { it == null || it.isBlank() }) {
            // It's probably a human error if only one of [path, password], better throw an error
            if (trustStoreProps.any { it != null }) throw IllegalArgumentException(
                    "Both or none of [trustStorePassword, trustStorePath] should be configured for node ${nodeParams.address}")
            // Warn if not using SSL for remote RPC connections
            if (listOf("localhost", "127.0.0.1").none { nodeParams.address!!.startsWith(it) })
                logger.warn("Not using SSL for RPC to remote node ${nodeParams.address}")
            null
        }
        // Config looks legit, pass it on
        else ClientRpcSslOptions(
                File(nodeParams.trustStorePath).toPath(),
                nodeParams.trustStorePassword!!,
                nodeParams.trustStoreProvider)

    }

    /** Build a [NetworkHostAndPort] from the configuration host and port */
    private fun buildRpcAddress(): NetworkHostAndPort {
        val addressParts = nodeParams.address!!.split(":")
        val rpcAddress = NetworkHostAndPort(addressParts[0], addressParts[1].toInt())
        return rpcAddress
    }

    /** Build a [CordaRPCClientConfiguration] based on the provided [NodeParams] */
    private fun buildRpcClientConfig(): CordaRPCClientConfiguration {
        var cordaRPCClientConfiguration = CordaRPCClientConfiguration(
                connectionMaxRetryInterval = nodeParams.connectionMaxRetryInterval!!,
                connectionRetryInterval = nodeParams.connectionRetryInterval!!,
                connectionRetryIntervalMultiplier = nodeParams.connectionRetryIntervalMultiplier!!,
                deduplicationCacheExpiry = nodeParams.deduplicationCacheExpiry!!,
                maxFileSize = nodeParams.maxFileSize!!,
                maxReconnectAttempts = nodeParams.maxReconnectAttempts!!,
                observationExecutorPoolSize = nodeParams.observationExecutorPoolSize!!,
                reapInterval = nodeParams.reapInterval!!,
                trackRpcCallSites = nodeParams.trackRpcCallSites!!,
                minimumServerProtocolVersion = nodeParams.minimumServerProtocolVersion!!
        )
        return cordaRPCClientConfiguration
    }

    /** Try cleaning up on [PreDestroy] */
    @PreDestroy
    fun onPreDestroy() {
        try{
            if (::rpcConnection.isInitialized) rpcConnection.notifyServerAndClose()
        }
        catch (e: Throwable){
            logger.warn("Error notifying server ${nodeParams.address}", e)
        }
    }

    /** Controls ignoring this node for Actuator */
    override fun skipInfo(): Boolean = this.nodeParams.skipInfo ?: false
}
