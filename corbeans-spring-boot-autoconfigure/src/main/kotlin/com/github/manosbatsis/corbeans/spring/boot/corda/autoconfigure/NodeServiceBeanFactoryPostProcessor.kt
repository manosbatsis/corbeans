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
package com.github.manosbatsis.corbeans.spring.boot.corda.autoconfigure


import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.util.EagerNodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.util.LazyNodeRpcConnection
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
 * Dynamically creates and registers CordaNodeService beans based on
 * the Spring Boot application configuration
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
        logger.debug("Loaded corda nodes config: {}", cordaNodesProperties)
    }

    /**
     * Create and register a [CordaNodeService] bean per node according to configuration
     */
    @Throws(BeansException::class)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val beanDefinitionRegistry = beanFactory as BeanDefinitionRegistry

        this.cordaNodesProperties.nodes.forEach{ (nodeName, nodeParams) ->
            logger.debug("postProcessBeanFactory, nodeName: {}, nodeParams: {}", nodeName, nodeParams)
            System.out.println("postProcessBeanFactory, nodeName: ${nodeName}, nodeParams: ${nodeParams}")

            // register RPC connection wrapper bean
            val rpcConnectionBeanName = "${nodeName}RpcConnection";
            val rpcConnectionBean = BeanDefinitionBuilder
                    .rootBeanDefinition(if(nodeParams.lazy) LazyNodeRpcConnection::class.java else EagerNodeRpcConnection::class.java)
                    .setScope(SCOPE_SINGLETON)
                    .addConstructorArgValue(nodeParams)
                    .getBeanDefinition()
            beanDefinitionRegistry.registerBeanDefinition(rpcConnectionBeanName, rpcConnectionBean)
            logger.debug("Registered RPC connection bean {} for Party {}", rpcConnectionBeanName, nodeName)
            System.out.println("Registered RPC connection bean ${rpcConnectionBeanName} for Party ${nodeName}")

            // verify node service type
            val serviceType = Class.forName(nodeParams.primaryServiceType)
            if(!CordaNodeServiceImpl::class.java.isAssignableFrom(serviceType)){
                throw IllegalArgumentException("Provided service type for node ${nodeName} does not extend CordaNodeServiceImpl or CordaNodeServiceImpl")
            }
            // register Node service
            val nodeServiceBeanName = "${nodeName}NodeService";
            val nodeServiceBean = BeanDefinitionBuilder
                    // TODO: make service class configurable
                    .rootBeanDefinition(serviceType)
                    .addConstructorArgReference(rpcConnectionBeanName)
                    .getBeanDefinition()
            beanDefinitionRegistry.registerBeanDefinition(nodeServiceBeanName, nodeServiceBean)
            logger.debug("Registered node service {} for Party: {}, type: {}", nodeServiceBeanName, nodeName, serviceType.name)
            System.out.println("Registered node service ${nodeServiceBeanName} for Party: ${nodeName}, type: ${serviceType.name}")
        }
    }

    /**
     * Parse spring-boot config files to a [CordaNodesProperties] instance
     */
    fun buildCordaNodesProperties(environment: ConfigurableEnvironment): CordaNodesProperties {
        val sources = ConfigurationPropertySources.from(environment.propertySources)
        val binder = Binder(sources)
        return binder.bind("corbeans", CordaNodesProperties::class.java).orElseCreate(CordaNodesProperties::class.java)
    }
}
