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
package com.github.manosbatsis.corbeans.spring.boot.corda.autoconfigure

import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeService
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.util.EagerNodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.util.LazyNodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeParams
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanDefinition.SCOPE_SINGLETON
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.EnvironmentAware
import org.springframework.core.annotation.Order
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment


/**
 * Dynamically creates and registers [NodeRpcConnection] and [CordaNodeService] beans based on
 * the node or Spring Boot application configuration, i.e. `node.conf` and `application.properties`
 * respectively
 */
@Order
open class NodeServiceBeanFactoryPostProcessor : BeanFactoryPostProcessor, EnvironmentAware {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeServiceBeanFactoryPostProcessor::class.java)
    }

    protected lateinit var cordaNodesProperties: CordaNodesProperties

    /**
     * Parse spring-boot config manually since it's not available yet
     */
    override fun setEnvironment(env: Environment) {
        this.cordaNodesProperties = this.buildCordaNodesProperties(env as ConfigurableEnvironment)
        logger.info("Loaded config for Corda nodes: {}", cordaNodesProperties.nodes.keys)
    }

    /**
     * Create and register a [CordaNodeService] bean per node according to configuration
     */
    @Throws(BeansException::class)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val beanDefinitionRegistry = beanFactory as BeanDefinitionRegistry
        // Pickup any user-defined global defaultParams, use defaults if missing
        val defaultParams = this.cordaNodesProperties.nodes.getOrDefault(NodeParams.NODENAME_DEFAULT, NodeParams.DEFAULT)
        // Process node connection definitions
        this.cordaNodesProperties.nodes.forEach{ (nodeName, partialParams) ->
            // Ignore "default" overrides
            if(nodeName != NodeParams.NODENAME_DEFAULT){
                logger.debug("Registering, node name: {}, address: {}", nodeName, partialParams.address)
                // Merge params to complete config
                val nodeParams = NodeParams.mergeParams(partialParams, defaultParams)
                // register RPC connection wrapper bean
                registerConnectionWrapper(nodeParams, nodeName, beanDefinitionRegistry)
                // register Node service
                registerNodeService(nodeParams, nodeName, beanDefinitionRegistry)
            }
        }
    }

    /** Register Node service */
    private fun registerNodeService(nodeParams: NodeParams, nodeName: String, beanDefinitionRegistry: BeanDefinitionRegistry) {
        // Convention-based rpc wrapper bean name
        val rpcConnectionBeanName = "${nodeName}RpcConnection";
        // Verify the node service type
        val serviceType = verifyNodeServiceType(nodeParams, nodeName)
        val nodeServiceBeanName = "${nodeName}NodeService";
        val nodeServiceBean = BeanDefinitionBuilder
                .rootBeanDefinition(serviceType)
                .addConstructorArgReference(rpcConnectionBeanName)
                .getBeanDefinition()
        beanDefinitionRegistry.registerBeanDefinition(nodeServiceBeanName, nodeServiceBean)
        logger.debug("Registered node service {} for Party: {}, type: {}", nodeServiceBeanName, nodeName, serviceType.name)
    }

    /** Verify node service type */
    private fun verifyNodeServiceType(nodeParams: NodeParams, nodeName: String): Class<*> {
        // Verify interface implementation
        val serviceType = Class.forName(nodeParams.primaryServiceType)
        if (!CordaNodeService::class.java.isAssignableFrom(serviceType)) {
            throw IllegalArgumentException("Provided service type for node ${nodeName} does not implement CordaNodeService")
        }
        return serviceType
    }

    /** Register RPC connection wrapper bean */
    private fun registerConnectionWrapper(nodeParams: NodeParams, nodeName: String, beanDefinitionRegistry: BeanDefinitionRegistry): String {
        val rpcConnectionBeanName = "${nodeName}RpcConnection"
        val rpcConnectionBean = BeanDefinitionBuilder
                .rootBeanDefinition(if (nodeParams.lazy!!) LazyNodeRpcConnection::class.java else EagerNodeRpcConnection::class.java)
                .setScope(SCOPE_SINGLETON)
                .addConstructorArgValue(nodeParams)
                .getBeanDefinition()
        beanDefinitionRegistry.registerBeanDefinition(rpcConnectionBeanName, rpcConnectionBean)
        logger.debug("Registered RPC connection bean {} for Party {}", rpcConnectionBeanName, nodeName)
        return rpcConnectionBeanName
    }

    /**
     * Parse spring-boot config files to a [CordaNodesProperties] instance,
     * as it's too soon for spring to have application properties available
     */
    fun buildCordaNodesProperties(environment: ConfigurableEnvironment): CordaNodesProperties {
        val sources = ConfigurationPropertySources.from(environment.propertySources)
        val binder = Binder(sources)
        return binder.bind("corbeans", CordaNodesProperties::class.java).orElseCreate(CordaNodesProperties::class.java)
    }

}
