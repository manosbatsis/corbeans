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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * An [RpcConfigurationService] implementation that uses application.properties
 * to determine configuration for nodes. Sample config:
 *
 * ```properties
 * # Cordapp packages
 * corbeans.cordapPackages=\
 *     foo.bar.baz.cordapp.contract,\
 *     foo.bar.baz.cordapp.workflow, \
 *     com.github.manosbatsis.vaultaire.dto, \
 *     com.github.manosbatsis.vaultaire.plugin.accounts.dto, \
 *     com.github.manosbatsis.partiture.flow, \
 *     com.r3.corda.lib.accounts.contracts, \
 *     com.r3.corda.lib.accounts.workflows, \
 *     com.r3.corda.lib.tokens.contracts, \
 *     com.r3.corda.lib.tokens.workflows, \
 *     com.r3.corda.lib.tokens.selection, \
 *     com.r3.corda.lib.ci.workflows, \
 *     net.corda.bn.contracts, \
 *     com.r3.businessnetworks.membership.flows
 *
 * # First node
 * corbeans.nodes.partyA.partyName=O\=PartyA, L\=London, C\=GB
 * corbeans.nodes.partyA.username=user1
 * corbeans.nodes.partyA.password=test
 * corbeans.nodes.partyA.address=localhost:10006
 * corbeans.nodes.partyA.adminAddress=localhost:10046
 *
 * # Second node
 * corbeans.nodes.partyB.partyName=O\=PartyB, L\=London, C\=GB
 * corbeans.nodes.partyB.username=user1
 * corbeans.nodes.partyB.password=test
 * corbeans.nodes.partyB.address=localhost:10009
 * corbeans.nodes.partyB.adminAddress=localhost:10049
 * ```
 */
open class ApplicationPropertiesBasedRpcConfigurationService :
        RpcConfigurationService {

    companion object {
        private val logger = LoggerFactory.getLogger(ApplicationPropertiesBasedRpcConfigurationService::class.java)
    }

    /** Load config from application.properties */
    @Autowired
    lateinit private var cordaNodesProperties: CordaNodesProperties

    val poolParams: PoolParams by lazy {
        PoolParams.mergeParams(this.cordaNodesProperties.poolParams, PoolParams.DEFAULT)
    }

    val rpcNodeParams: MutableMap<String, NodeParams> by lazy {
        // Pickup any user-defined global defaultParams, use defaults if missing
        val defaultParams = cordaNodesProperties.nodes
                .getOrDefault(NodeParams.NODENAME_DEFAULT, NodeParams.DEFAULT)
        val rpcNodeParams = mutableMapOf<String, NodeParams>()
        // Get the custom SerializationCustomSerializer
        // implementations to register with our RPC ops
        val customSerializers = getCustomSerializers(cordaNodesProperties.cordapPackages)

        // Process node connection definitions
        this.cordaNodesProperties.nodes
                // Ignore "default" overrides
                .filter { it.key != NodeParams.NODENAME_DEFAULT }
                .forEach { (nodeName, partialParams) ->
                    logger.debug("Registering, node name: {}, params: {}", nodeName, partialParams)
                    // Merge params to complete config
                    val nodeParams = NodeParams.mergeParams(partialParams, defaultParams)
                    // Update custom serializers if not already set
                    if (nodeParams.customSerializers != null
                            && nodeParams.customSerializers!!.isEmpty())
                        nodeParams.customSerializers = customSerializers
                    // "register" RPC connection config
                    rpcNodeParams[nodeName] = nodeParams
                }
        rpcNodeParams
    }

    override fun getAllRpcNodeParams(): Map<String, NodeParams> =
            rpcNodeParams.map { it.key to it.value }.toMap()

    override fun getRpcNodeParams(nodeName: String): NodeParams {
        return getAllRpcNodeParams()[nodeName]
                ?: error("${RpcConfigurationService.rpcConnectionForNodeNotFound} $nodeName")
    }

    override fun getRpcPoolParams(): PoolParams {
        return poolParams
    }

    override fun buildPoolKey(nodeName: String): PoolKey {
        return PoolKey(nodeName = nodeName)
    }

}
