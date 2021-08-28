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
import com.github.manosbatsis.corda.rpc.poolboy.PoolBoy
import com.github.manosbatsis.corda.rpc.poolboy.PoolBoyConnection
import com.github.manosbatsis.corda.rpc.poolboy.PoolBoyPooledConnection
import com.github.manosbatsis.corda.rpc.poolboy.PoolKey
import com.github.manosbatsis.corda.rpc.poolboy.config.RpcConfigurationService
import com.github.manosbatsis.vaultaire.annotation.ExtendedStateServiceBean
import com.github.manosbatsis.vaultaire.dto.info.NetworkInfo
import com.github.manosbatsis.vaultaire.plugin.accounts.service.dao.ExtendedAccountsAwareStateService
import com.github.manosbatsis.vaultaire.registry.Registry
import com.github.manosbatsis.vaultaire.service.ServiceDefaults
import com.github.manosbatsis.vaultaire.service.SimpleServiceDefaults
import com.github.manosbatsis.vaultaire.service.dao.ExtendedStateService
import net.corda.core.contracts.ContractState
import net.corda.core.identity.CordaX500Name
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.InitializingBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider
import org.springframework.core.GenericTypeResolver
import org.springframework.core.type.filter.AnnotationTypeFilter
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

    protected open val stateServiceInterface: Class<out ExtendedStateService<*, *, *, *>> =
            ExtendedStateService::class.java

    /** Maintain an RPC client/ops pool */
    lateinit var poolBoy: PoolBoy

    /** Load config from application.properties */
    @Autowired
    lateinit private var cordaNodesProperties: CordaNodesProperties

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
        // Init poolboy
        this.poolBoy = PoolBoy(rpcConfigurationService)

        // Init state service registry
        val scanner = ClassPathScanningCandidateComponentProvider(false)
        scanner.addIncludeFilter(AnnotationTypeFilter(ExtendedStateServiceBean::class.java))
        cordaNodesProperties.cordappPackages.forEach{packageName ->
            for (beanDefinition in scanner.findCandidateComponents(packageName)) {
                try {
                    val serviceClass = Class.forName(beanDefinition.beanClassName)
                            as Class<out ExtendedStateService<*, *, *, *>>
                    val stateType = GenericTypeResolver
                            .resolveTypeArgument(serviceClass, stateServiceInterface)
                            as Class<out ContractState>
                    Registry.registerService(stateType, serviceClass)
                } catch (e: ClassNotFoundException) {
                    logger.warn(
                            "Could not resolve class object for state service type", e)
                }
            }
        }
    }

    override fun getInfo(): NetworkInfo {
        return NetworkInfo(getNodesInfo())
    }

    override fun getNodesInfo() = this.rpcConfigurationService
            .getAllRpcNodeParams().mapNotNull {
                val nodeKey = PoolKey(it.key)
                val rpcConnection = this.poolBoy.borrowConnection(nodeKey)
                val nodeInfoEntry = if (!rpcConnection.skipInfo()) {
                    it.key to CordaNodeServiceImpl(poolBoy.forKey(nodeKey))
                            .getExtendedInfo()
                } else null
                this.poolBoy.returnConnection(nodeKey, rpcConnection)
                nodeInfoEntry
            }
            .toMap()

    override fun getNodeRpcPool(optionalNodeName: Optional<String>): PoolBoyConnection {
        val nodeName = resolveNodeName(optionalNodeName)
        val poolKey = rpcConfigurationService.buildPoolKey(nodeName)
        return poolBoy.forKey(poolKey)
    }

    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        return CordaNodeServiceImpl(getNodeRpcPool(optionalNodeName))
    }

    private val serviceConstructors = mutableMapOf<String, Constructor<*>>()

    override fun <T> getService(serviceType: Class<T>, optionalNodeName: Optional<String>): T {
        val nodeName = resolveNodeName(optionalNodeName)
        val poolKey = PoolKey(nodeName)
        val constructor = serviceConstructors.getOrPut(serviceType.canonicalName){
            serviceType.constructors.find {
                logger.debug("constructor parameter types: ${it.parameterTypes.joinToString(",") { it.canonicalName }}")
                if(it.parameterTypes.size == 1){
                    hasPoolBoyFirstParam(it)
                }
                else false
            } ?: error("No constructor found with appropriate parameters " +
                    "(${PoolBoyPooledConnection::class.java.simpleName}, " +
                    "for class ${serviceType.canonicalName}")
        }
        return with(constructor){
             newInstance(poolBoy.forKey(poolKey)) as T

        }
    }

    private fun hasPoolBoyFirstParam(it: Constructor<*>) =
            it.parameterTypes.first().isAssignableFrom(PoolBoyConnection::class.java)

    override fun resolveNodeName(optionalNodeName: Optional<String>): String {
        val nodeName = (if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName)
                // "Normalise" string if X500 name
                ?.let{
                    if(it.contains("=")) try{ CordaX500Name.parse(it).toString() } catch (e: Exception){ it }
                    else it
                }
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
                    CordaNodeServiceImpl(poolBoy.forKey(PoolKey(it.key)))
                            .refreshNetworkMapCache()
                }
    }
}



/**
 * Accounts-aware Corda network service
 */
class CordaAccountsAwareNetworkServiceImpl : CordaNetworkServiceImpl(){
    override val stateServiceInterface: Class<out ExtendedStateService<*, *, *, *>> = ExtendedAccountsAwareStateService::class.java

    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        return CordaAccountsAwareNodeServiceImpl(getNodeRpcPool(optionalNodeName))
    }
}