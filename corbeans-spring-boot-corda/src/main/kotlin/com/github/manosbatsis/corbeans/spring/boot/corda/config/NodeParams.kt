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
package com.github.manosbatsis.corbeans.spring.boot.corda.config

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeServiceImpl
import net.corda.client.rpc.CordaRPCClientConfiguration
import java.time.Duration

/**
 * Configuration of a single node from a corbeans perspective.
 * Includes information corresponding to an RPC user credentials and an [CordaRPCClientConfiguration].
 * The `DEFAULTS` are taken from [CordaRPCClientConfiguration.DEFAULT] and can be overridden
 * using `corbeans.nodes.default.xx` in your `application.properties`
 */
class NodeParams {

    companion object {
        @JvmStatic
        val NODENAME_DEFAULT = "default"
        @JvmStatic
        val NODENAME_CORDFORM = "cordform"
        @JvmStatic
        val DEFAULT = NodeParams()
        init {
            DEFAULT.eager = false
            DEFAULT.primaryServiceType = CordaNodeServiceImpl::class.java.canonicalName
            DEFAULT.bnmsServiceType = null
            DEFAULT.connectionMaxRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionMaxRetryInterval
            DEFAULT.connectionRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionRetryInterval
            DEFAULT.connectionRetryIntervalMultiplier = CordaRPCClientConfiguration.DEFAULT.connectionRetryIntervalMultiplier
            DEFAULT.deduplicationCacheExpiry = CordaRPCClientConfiguration.DEFAULT.deduplicationCacheExpiry
            DEFAULT.maxFileSize = CordaRPCClientConfiguration.DEFAULT.maxFileSize
            DEFAULT.maxReconnectAttempts = CordaRPCClientConfiguration.DEFAULT.maxReconnectAttempts
            DEFAULT.minimumServerProtocolVersion = CordaRPCClientConfiguration.DEFAULT.minimumServerProtocolVersion
            DEFAULT.observationExecutorPoolSize = CordaRPCClientConfiguration.DEFAULT.observationExecutorPoolSize
            DEFAULT.reapInterval = CordaRPCClientConfiguration.DEFAULT.reapInterval
            DEFAULT.trackRpcCallSites = CordaRPCClientConfiguration.DEFAULT.trackRpcCallSites
            DEFAULT.skipInfo = false
        }

        /** Merge in order of precedence, with NodeParams.DEFAULT being the implicit last option */
        @JvmStatic
        fun mergeParams(partialParams: NodeParams, defaultParams: NodeParams): NodeParams {
            val nodeParams = NodeParams()
            nodeParams.username = partialParams.username ?: throw IllegalArgumentException("Node configuration is missing a username property")
            nodeParams.password = partialParams.password ?: throw IllegalArgumentException("Node configuration is missing a password property")
            nodeParams.address = partialParams.address ?: throw IllegalArgumentException("Node configuration is missing an address property")
            nodeParams.adminAddress = partialParams.adminAddress ?: throw IllegalArgumentException("Node configuration is missing an adminAddress property")
            nodeParams.eager = partialParams.eager ?: defaultParams.eager ?: DEFAULT.eager!!
            nodeParams.primaryServiceType = partialParams.primaryServiceType ?: defaultParams.primaryServiceType ?: DEFAULT.primaryServiceType!!
            nodeParams.bnmsServiceType = partialParams.bnmsServiceType ?: defaultParams.bnmsServiceType
                    ?: DEFAULT.bnmsServiceType
            nodeParams.connectionMaxRetryInterval = partialParams.connectionMaxRetryInterval ?: defaultParams.connectionMaxRetryInterval ?: DEFAULT.connectionMaxRetryInterval!!
            nodeParams.connectionRetryInterval = partialParams.connectionRetryInterval ?: defaultParams.connectionRetryInterval ?: DEFAULT.connectionRetryInterval!!
            nodeParams.connectionRetryIntervalMultiplier = partialParams.connectionRetryIntervalMultiplier ?: defaultParams.connectionRetryIntervalMultiplier ?: DEFAULT.connectionRetryIntervalMultiplier!!
            nodeParams.deduplicationCacheExpiry = partialParams.deduplicationCacheExpiry ?: defaultParams.deduplicationCacheExpiry ?: DEFAULT.deduplicationCacheExpiry!!
            nodeParams.maxFileSize = partialParams.maxFileSize ?: defaultParams.maxFileSize ?: DEFAULT.maxFileSize!!
            nodeParams.maxReconnectAttempts = partialParams.maxReconnectAttempts ?: defaultParams.maxReconnectAttempts ?: DEFAULT.maxReconnectAttempts!!
            nodeParams.minimumServerProtocolVersion = partialParams.minimumServerProtocolVersion ?: defaultParams.minimumServerProtocolVersion ?: DEFAULT.minimumServerProtocolVersion!!
            nodeParams.observationExecutorPoolSize = partialParams.observationExecutorPoolSize ?: defaultParams.observationExecutorPoolSize ?: DEFAULT.observationExecutorPoolSize!!
            nodeParams.reapInterval = partialParams.reapInterval ?: defaultParams.reapInterval ?: DEFAULT.reapInterval!!
            nodeParams.trackRpcCallSites = partialParams.trackRpcCallSites ?: defaultParams.trackRpcCallSites ?: DEFAULT.trackRpcCallSites!!
            nodeParams.eager = partialParams.eager ?: defaultParams.eager ?: DEFAULT.eager!!
            nodeParams.skipInfo = partialParams.skipInfo ?: defaultParams.skipInfo ?: DEFAULT.skipInfo!!

            return nodeParams
        }
    }
    /** RPC user */
    var username: String? = null
    /** RPC user password */
    var password: String? = null
    /** Node RPC address */
    var address: String? = null
    /** Node administration RPC address */
    var adminAddress: String? = null
    /**
     * Whether to use an eagerly initialised [NodeRpcConnection] implementation.
     * Default is `false`. Setting to `true` will break integration tests so you will need separate
     * application.properties for those.
     */
    var eager: Boolean? = null
    /** The [CordaNodeService] implementation to use when creating and registering the corresponding bean */
    var primaryServiceType: String? = null
    /** The [CordaBnmsService] implementation to use when creating and registering the corresponding bean */
    var bnmsServiceType: String? = null
    /** Corresponds to [ClientRpcSslOptions.trustStorePath] */
    var trustStorePath: String? = null
    /** Corresponds to [ClientRpcSslOptions.trustStorePassword] */
    var trustStorePassword: String? = null
    /** Corresponds to [ClientRpcSslOptions.trustStoreProvider] */
    var trustStoreProvider: String = "JKS"

    // Configuration properties for Corda v4.0+
    // ====================================

    /**
     * The maximum retry interval for re-connections. The client will retry connections if the host is lost with ever
     * increasing spacing until the max is reached. The default is 3 minutes.
     */
    var connectionMaxRetryInterval: Duration? = null
    /*** The base retry interval for reconnection attempts. The default is 5 seconds. */
    var connectionRetryInterval: Duration? = null
    /** The retry interval multiplier for exponential backoff. The default is 1.5 */
    var connectionRetryIntervalMultiplier: Double? = null
    /** The cache expiry of a deduplication watermark per client. Default is 1 day. */
    var deduplicationCacheExpiry: Duration? = null
    /** Maximum size of RPC responses, in bytes. Default is 10mb. */
    var maxFileSize: Int? = null
    /** Maximum reconnect attempts on failover or disconnection. The default is -1 which means unlimited. */
    var maxReconnectAttempts: Int? = null
    /**
     * The minimum protocol version required from the server. This is equivalent to the node's platform version number.
     * If this minimum version is not met, an exception will be thrown at startup. If you use features introduced in a
     * later version, you can bump this to match the platform version you need and get an early check that runs
     * before you do anything.
     */
    var minimumServerProtocolVersion: Int? = null
    /**
     * The number of threads to use for observations for executing Observable.onNext. This only has any effect if
     * observableExecutor is null (which is the default). The default is 4.
     */
    var observationExecutorPoolSize: Int? = null
    /**
     * The interval of unused observable reaping. Leaked Observables (unused ones) are detected using weak references and
     * are cleaned up in batches in this interval. If set too large it will waste server side resources for this duration.
     * If set too low it wastes client side cycles. The default is to check once per second.
     */
    var reapInterval: Duration? = null
    /**
     * If set to true the client will track RPC call sites (default is false). If an error occurs subsequently during the RPC
     * or in a returned Observable stream the stack trace of the originating RPC will be shown as well. Note that
     * constructing call stacks is a moderately expensive operation.
     */
    var trackRpcCallSites: Boolean? = null
    /** Whether to skip this node from actuator */
    var skipInfo: Boolean? = null

    override fun toString(): String {
        return "NodeParams(address=$address, adminAddress=$adminAddress, eager=$eager, primaryServiceType=$primaryServiceType, connectionMaxRetryInterval=$connectionMaxRetryInterval, connectionRetryInterval=$connectionRetryInterval, connectionRetryIntervalMultiplier=$connectionRetryIntervalMultiplier)"
    }

}
