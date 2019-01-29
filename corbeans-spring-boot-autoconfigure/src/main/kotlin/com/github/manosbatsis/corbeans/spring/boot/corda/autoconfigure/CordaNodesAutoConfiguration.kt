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
package com.github.manosbatsis.corbeans.spring.boot.corda.autoconfigure


import com.github.manosbatsis.corbeans.spring.boot.corda.actuator.CordaInfoContributor
import com.github.manosbatsis.corbeans.spring.boot.corda.actuator.CordaInfoEndpoint
import com.github.manosbatsis.corbeans.spring.boot.corda.bind.CordaX500NameConverter
import com.github.manosbatsis.corbeans.spring.boot.corda.bind.SecureHashConverter
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import net.corda.client.jackson.JacksonSupport
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.jackson.JsonComponentModule
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import org.springframework.http.converter.json.SpringHandlerInstantiator


/**
 * Auto-configures a CordaNodeService for each Corda Node
 */
@Configuration
@ComponentScan(basePackages = arrayOf("com.github.manosbatsis.corbeans"))
class CordaNodesAutoConfiguration {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesAutoConfiguration::class.java)

        @Bean
        @ConditionalOnMissingBean(NodeServiceBeanFactoryPostProcessor::class)
        @JvmStatic
        fun nodeServiceBeanFactoryPostProcessor(): NodeServiceBeanFactoryPostProcessor {
            logger.debug("Creating a NodeServiceBeanFactoryPostProcessor")
            return NodeServiceBeanFactoryPostProcessor()
        }
    }

    @Autowired
    protected lateinit var applicationContext: ApplicationContext
    @Autowired
    protected lateinit var cordaNodesProperties: CordaNodesProperties
    @Autowired
    protected lateinit var networkService: CordaNetworkService

    /** Add transparent [net.corda.core.crypto.SecureHash] conversion */
    @Bean
    @ConditionalOnMissingBean(SecureHashConverter::class)
    fun secureHashConverter(): SecureHashConverter {
        return SecureHashConverter()
    }

    /** Add transparent [net.corda.core.identity.CordaX500Name] conversion */
    @Bean
    @ConditionalOnMissingBean(CordaX500NameConverter::class)
    fun cordaX500NameConverter(): CordaX500NameConverter {
        return CordaX500NameConverter()
    }

    /** Add custom actuator endpoint based on known nodes */
    @Bean
    fun cordaInfoEndpoint(): CordaInfoEndpoint {
        return CordaInfoEndpoint()
    }

    /** Extend actuator's info endpoint based on known nodes */
    @Bean
    @ConditionalOnProperty(
            prefix = "corbeans",
            name = arrayOf("actuator.info.disable"),
            havingValue="false",
            matchIfMissing = true)
    fun cordaInfoContributor(): CordaInfoContributor {
        return CordaInfoContributor()
    }

    /** Force Spring/Jackson to use a Corda ObjectMapper for (de)serialization */
    @Bean
    @ConditionalOnProperty(
            prefix = "corbeans",
            name = arrayOf("objectmapper.disable"),
            havingValue = "false",
            matchIfMissing = true)
    fun mappingJackson2HttpMessageConverter(
            @Autowired jsonComponentModule: JsonComponentModule
    ): MappingJackson2HttpMessageConverter {
        // Check if a service key has been configured for obtaining the proxy
        var proxyServiceKey = cordaNodesProperties.objectMapper.proxyServiceKey
        // Choose a random key if '*', ignoring default config for all nodes
        if (proxyServiceKey == "*") proxyServiceKey = cordaNodesProperties.nodes.keys.first { it != "default" }
        // Create a mapper
        val mapper = if (proxyServiceKey == null || proxyServiceKey.isBlank()) {
            logger.debug("Creating non-RPC Corda ObjectMapper")
            JacksonSupport.createNonRpcMapper(fullParties = cordaNodesProperties.objectMapper.fullParties)
        } else {
            logger.debug("Creating RPC Corda ObjectMapper using proxy from: $proxyServiceKey")
            JacksonSupport.createDefaultMapper(
                    rpc = networkService.getNodeService(proxyServiceKey).proxy(),
                    fullParties = cordaNodesProperties.objectMapper.fullParties,
                    fuzzyIdentityMatch = cordaNodesProperties.objectMapper.fuzzyIdentityMatch
            )
        }
        // This HandlerInstantiator will handle autowiring properties into the custom serializers
        mapper.setHandlerInstantiator(
                SpringHandlerInstantiator(this.applicationContext.autowireCapableBeanFactory))
        // Register the module
        mapper.registerModule(jsonComponentModule)
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }
}
