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

import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.bind.Binder
import org.springframework.boot.context.properties.source.ConfigurationPropertySources
import org.springframework.context.EnvironmentAware
import org.springframework.core.env.ConfigurableEnvironment
import org.springframework.core.env.Environment


/**
 * Dynamically creates and registers [NodeRpcConnection] and [CordaNodeService] beans based on
 * the node or Spring Boot application configuration, i.e. `node.conf` and `application.properties`
 * respectively
 */
abstract class AbstractBeanFactoryPostProcessor : EnvironmentAware {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractBeanFactoryPostProcessor::class.java)
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
     * Parse spring-boot config files to a [CordaNodesProperties] instance,
     * as it's too soon for spring to have application properties available
     */
    fun buildCordaNodesProperties(environment: ConfigurableEnvironment): CordaNodesProperties {
        val sources = ConfigurationPropertySources.from(environment.propertySources)
        val binder = Binder(sources)
        return binder.bind("corbeans", CordaNodesProperties::class.java).orElseCreate(CordaNodesProperties::class.java)
    }

}
