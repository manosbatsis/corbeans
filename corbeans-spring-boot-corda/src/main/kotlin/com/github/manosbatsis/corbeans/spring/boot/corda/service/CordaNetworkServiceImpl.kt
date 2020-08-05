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

import com.github.manosbatsis.corda.rpc.poolboy.PoolBoy
import com.github.manosbatsis.corda.rpc.poolboy.PoolBoyConnection
import com.github.manosbatsis.corda.rpc.poolboy.PoolBoyPooledConnection
import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.vaultaire.dto.info.NetworkInfo
import com.github.manosbatsis.vaultaire.service.ServiceDefaults
import com.github.manosbatsis.vaultaire.service.SimpleServiceDefaults
import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import java.lang.reflect.Constructor
import java.util.Optional


/**
 * Default Corda network service
 */
open class CordaNetworkServiceImpl :
        CordaNetworkService,
        InitializingBean{

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNetworkServiceImpl::class.java)
        private val SERVICE_NAME_SUFFIX = "NodeService"
    }

    /** Maintain an RPC client/ops pool */
    lateinit var rpcConnectionPool: PoolBoy

    @Autowired
    lateinit var rpcConfigurationService: RpcConfigurationService

    override val nodeNames: Set<String>
        get() = with(rpcConfigurationService) {
            getAllRpcNodeParams().keys
        }

    override val defaultNodeName: String?
        get() = with(rpcConfigurationService) {
            if (getAllRpcNodeParams().keys.size == 1)
                getAllRpcNodeParams().keys.single()
            else null
        }

    override val nodeNamesByOrgName by lazy {
        with(rpcConfigurationService) {
            getAllRpcNodeParams().map {
                val x500Name = it.value.partyName
                        ?: error("Missing property testPartyName")
                CordaX500Name.parse(x500Name).organisation to it.key
            }.toMap()
        }
    }

    override val nodeNamesByX500Name by lazy {
        with(rpcConfigurationService) {
            getAllRpcNodeParams().map {
                val x500Name = it.value.partyName
                        ?: error("Missing property testPartyName")
                CordaX500Name.parse(x500Name).toString() to it.key
            }.toMap()
        }
    }

    override fun afterPropertiesSet() {
        this.rpcConnectionPool = PoolBoy(rpcConfigurationService)
    }

    override fun getInfo(): NetworkInfo {
        return NetworkInfo(getNodesInfo())
    }

    override fun getNodesInfo() = this.rpcConfigurationService
            .getAllRpcNodeParams().mapNotNull {
                val nodeKey = PoolKey(it.key)
                val rpcConnection = this.rpcConnectionPool.borrowConnection(nodeKey)
                val nodeInfoEntry = if (!rpcConnection.skipInfo()) {
                    it.key to CordaNodeServiceImpl(rpcConnectionPool.forKey(nodeKey))
                            .getExtendedInfo()
                } else null
                this.rpcConnectionPool.returnConnection(nodeKey, rpcConnection)
                nodeInfoEntry
            }
            .toMap()

    override fun getNodeRpcPool(optionalNodeName: Optional<String>): PoolBoyPooledConnection {
        val nodeName = resolveNodeName(optionalNodeName)
        val poolKey = PoolKey(nodeName)
        return rpcConnectionPool.forKey(poolKey)
    }

    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        return CordaNodeServiceImpl(getNodeRpcPool(optionalNodeName))
    }

    val serviceConstructors = mutableMapOf<String, Constructor<*>>()

    override fun <T> getService(serviceType: Class<T>, optionalNodeName: Optional<String>): T {
        val nodeName = resolveNodeName(optionalNodeName)
        val poolKey = PoolKey(nodeName)
        val constructor = serviceConstructors.getOrPut(serviceType.canonicalName){
            serviceType.constructors.find {
                logger.debug("constructor parameter types: ${it.parameterTypes.joinToString(",") { it.canonicalName }}")
                if(it.parameterTypes.size == 1){
                    hasPoolBoyFirstParam(it)
                }
                else if(it.parameterTypes.size == 2){
                    hasPoolBoyFirstParam(it) && hasServiceDefaultsLastParam(it)
                }
                else false
            } ?: error("No constructor found with appropriate parameters " +
                    "(${PoolBoyPooledConnection::class.java.simpleName}, " +
                    "optional ${ServiceDefaults::class.java.simpleName}) " +
                    "for class ${serviceType.canonicalName}")
        }
        return with(constructor){
            if(parameterTypes.size == 1) newInstance(rpcConnectionPool.forKey(poolKey)) as T
            else newInstance(rpcConnectionPool.forKey(poolKey), SimpleServiceDefaults()) as T
        }
    }

    private fun hasPoolBoyFirstParam(it: Constructor<*>) =
            it.parameterTypes.first().isAssignableFrom(PoolBoyConnection::class.java)

    private fun hasServiceDefaultsLastParam(it: Constructor<*>) =
            it.parameterTypes.last().isAssignableFrom(SimpleServiceDefaults::class.java)


    override fun resolveNodeName(optionalNodeName: Optional<String>): String {
        var nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
                ?: throw IllegalArgumentException("No nodeName was given and a default one is not available")
        if (nodeName.isBlank()) throw IllegalArgumentException("nodeName cannot be an empty or blank string")
        // If organization name match
        val resolved = if (nodeNames.contains(nodeName)) nodeName
        else if (nodeNamesByOrgName.containsKey(nodeName)) nodeNamesByOrgName.getValue(nodeName)
        else if (nodeNamesByX500Name.containsKey(nodeName)) nodeNamesByX500Name.getValue(nodeName)
        else throw IllegalArgumentException("Failed resolving node name: ${nodeName}, " +
                "available node names: ${this.nodeNames}, " +
                "available org names: ${this.nodeNamesByOrgName}, " +
                "available X500 names: ${this.nodeNamesByX500Name}")
        logger.debug("resolveNodeName, nodeName: $nodeName, resolved: $resolved")
        return resolved
    }

    override fun refreshNetworkMapCaches() {
        rpcConfigurationService
                .getAllRpcNodeParams()
                .forEach {
                    CordaNodeServiceImpl(rpcConnectionPool.forKey(PoolKey(it.key)))
                            .refreshNetworkMapCache()
                }
    }

}
