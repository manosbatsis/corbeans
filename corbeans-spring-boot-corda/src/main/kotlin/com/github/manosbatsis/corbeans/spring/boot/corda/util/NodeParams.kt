/**
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
package com.github.manosbatsis.corbeans.spring.boot.corda.util

import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
import net.corda.client.rpc.CordaRPCClientConfiguration

/**
 * Configuration of a single node from a corbeans perspective.
 * Includes information corresponding to an RPC user credentials and an [CordaRPCClientConfiguration].
 * Uses defaults from [CordaRPCClientConfiguration.DEFAULT].
 */
class NodeParams {

    /** RPC user */
    var username: String? = null
    /** RPC user password */
    var password: String? = null
    /** Node RPC address */
    var address: String? = null
    /** Node administration RPC address */
    var adminAddress: String? = null
    /** Whether to use a lazily initialised [NodeRpcConnection] implementation */
    var lazy: Boolean = false
    /** The [CordaNodeService] implementation to use when creating and registering the corresponding bean */
    var primaryServiceType = CordaNodeServiceImpl::class.java.canonicalName

    // Configuration properties for Corda v4.0+
    /**
     * The maximum retry interval for re-connections. The client will retry connections if the host is lost with ever
     * increasing spacing until the max is reached. The default is 3 minutes.
     */
    var connectionMaxRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionMaxRetryInterval

    /*** The base retry interval for reconnection attempts. The default is 5 seconds. */
    var connectionRetryInterval = CordaRPCClientConfiguration.DEFAULT.connectionRetryInterval

    /** The retry interval multiplier for exponential backoff. The default is 1.5 */
    var connectionRetryIntervalMultiplier = CordaRPCClientConfiguration.DEFAULT.connectionRetryIntervalMultiplier

    /** The cache expiry of a deduplication watermark per client. Default is 1 day. */
    var deduplicationCacheExpiry = CordaRPCClientConfiguration.DEFAULT.deduplicationCacheExpiry

    /** Maximum size of RPC responses, in bytes. Default is 10mb. */
    var maxFileSize = CordaRPCClientConfiguration.DEFAULT.maxFileSize

    /** Maximum reconnect attempts on failover or disconnection. The default is -1 which means unlimited. */
    var maxReconnectAttempts = CordaRPCClientConfiguration.DEFAULT.maxReconnectAttempts

    /**
     * The minimum protocol version required from the server. This is equivalent to the node's platform version number.
     * If this minimum version is not met, an exception will be thrown at startup. If you use features introduced in a
     * later version, you can bump this to match the platform version you need and get an early check that runs
     * before you do anything.
     */
    var minimumServerProtocolVersion = CordaRPCClientConfiguration.DEFAULT.minimumServerProtocolVersion

    /**
     * The number of threads to use for observations for executing Observable.onNext. This only has any effect if
     * observableExecutor is null (which is the default). The default is 4.
     */
    var observationExecutorPoolSize: Int = CordaRPCClientConfiguration.DEFAULT.observationExecutorPoolSize

    /**
     * The interval of unused observable reaping. Leaked Observables (unused ones) are detected using weak references and
     * are cleaned up in batches in this interval. If set too large it will waste server side resources for this duration.
     * If set too low it wastes client side cycles. The default is to check once per second.
     */
    var reapInterval = CordaRPCClientConfiguration.DEFAULT.reapInterval

    /**
     * If set to true the client will track RPC call sites (default is false). If an error occurs subsequently during the RPC
     * or in a returned Observable stream the stack trace of the originating RPC will be shown as well. Note that
     * constructing call stacks is a moderately expensive operation.
     */
    var trackRpcCallSites = CordaRPCClientConfiguration.DEFAULT.trackRpcCallSites


}