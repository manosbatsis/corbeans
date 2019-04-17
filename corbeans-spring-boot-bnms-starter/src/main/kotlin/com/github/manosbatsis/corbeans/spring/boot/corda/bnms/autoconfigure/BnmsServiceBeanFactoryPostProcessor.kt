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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.autoconfigure

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.service.CordaBnmsService
import com.github.manosbatsis.corbeans.spring.boot.corda.config.AbstractBeanFactoryPostProcessor
import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import org.slf4j.LoggerFactory
import org.springframework.beans.BeansException
import org.springframework.beans.factory.config.BeanFactoryPostProcessor
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.beans.factory.support.BeanDefinitionBuilder
import org.springframework.beans.factory.support.BeanDefinitionRegistry


/**
 * Dynamically creates and registers [CordaBnmsService] beans based on
 * the node or Spring Boot application configuration, i.e. `node.conf` and `application.properties`
 * respectively
 */
open class BnmsServiceBeanFactoryPostProcessor : AbstractBeanFactoryPostProcessor(), BeanFactoryPostProcessor {

    companion object {
        private val logger = LoggerFactory.getLogger(BnmsServiceBeanFactoryPostProcessor::class.java)
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
        this.cordaNodesProperties.nodes.forEach { (nodeName, partialParams) ->
            // Ignore "default" overrides
            if (nodeName != NodeParams.NODENAME_DEFAULT) {
                logger.debug("Registering, BNMS for node name: {}, address: {}", nodeName, partialParams.address)
                // Merge params to complete config
                val nodeParams = NodeParams.mergeParams(partialParams, defaultParams)
                // register BNMS service
                registerBnmsService(nodeParams, nodeName, beanDefinitionRegistry)
                logger.debug("PRocessed, BNMS for node name: {}", nodeName)
            } else logger.warn("Skipping, BNMS config for default node name: {}", nodeName)

        }

    }

    /** Register a Corda BNMS service */
    private fun registerBnmsService(nodeParams: NodeParams, nodeName: String, beanDefinitionRegistry: BeanDefinitionRegistry) {
        // Convention-based rpc wrapper bean name
        val rpcConnectionBeanName = "${nodeName}RpcConnection";
        // Verify the node service type
        val serviceType = verifyBnmsServiceType(nodeParams, nodeName)
        val nodeServiceBeanName = "${nodeName}BmnsService"
        if (serviceType != null) {
            val nodeServiceBean = BeanDefinitionBuilder
                    .rootBeanDefinition(serviceType)
                    .addConstructorArgReference(rpcConnectionBeanName)
                    .getBeanDefinition()
            beanDefinitionRegistry.registerBeanDefinition(nodeServiceBeanName, nodeServiceBean)
            logger.debug("Registered BNMS service {} for node name: {}, type: {}", nodeServiceBeanName, nodeName, serviceType.name)
        } else logger.warn("No BNMS service type registered for node name: {}, ignoring", nodeName)
    }

    /** Verify node service type */
    private fun verifyBnmsServiceType(nodeParams: NodeParams, nodeName: String): Class<*>? {
        // Verify interface implementation
        val serviceTypeName = nodeParams.bnmsServiceType
        val serviceType = if (serviceTypeName != null) {
            Class.forName(nodeParams.bnmsServiceType)
        } else null
        if (serviceType != null && !CordaBnmsService::class.java.isAssignableFrom(serviceType)) {
            throw IllegalArgumentException("Provided BNMS service type for node ${nodeName} does not implement CordaBnmsService")
        }
        return serviceType
    }


}
