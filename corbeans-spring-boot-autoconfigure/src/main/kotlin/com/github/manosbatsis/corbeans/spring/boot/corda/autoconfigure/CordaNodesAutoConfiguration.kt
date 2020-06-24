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
import com.github.manosbatsis.corbeans.spring.boot.corda.bind.*
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder
import org.springframework.http.converter.json.SpringHandlerInstantiator


/**
 * Auto-configures Corbeans
 */
@Configuration
@ComponentScan(basePackages = ["com.github.manosbatsis.corbeans"])
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
    @ConditionalOnMissingBean(name = ["stringToSecureHashConverter"])
    fun stringToSecureHashConverter(): StringToSecureHashConverter {
        return StringToSecureHashConverter()
    }
    @Bean
    @ConditionalOnMissingBean(name = ["secureHashToStringConverter"])
    fun secureHashToStringConverter(): SecureHashToStringConverter {
        return SecureHashToStringConverter()
    }

    /** Add transparent [net.corda.core.identity.CordaX500Name] conversion */
    @Bean
    @ConditionalOnMissingBean(name = ["cordaX500NameConverter"])
    fun cordaX500NameConverter(): StringToCordaX500NameConverter {
        return StringToCordaX500NameConverter()
    }
    @Bean
    @ConditionalOnMissingBean(name = ["cordaX500NameToStringConverter"])
    fun cordaX500NameToStringConverter(): CordaX500NameToStringConverter {
        return CordaX500NameToStringConverter()
    }


    /** Add transparent [net.corda.core.contracts.UniqueIdentifier] conversion */
    @Bean
    @ConditionalOnMissingBean(name = ["uniqueIdentifierConverter"])
    fun uniqueIdentifierConverter(): StringToUniqueIdentifierConverter {
        return StringToUniqueIdentifierConverter()
    }
    @Bean
    @ConditionalOnMissingBean(name = ["uniqueIdentifierToStringConverter"])
    fun uniqueIdentifierToStringConverter(): UniqueIdentifierToStringConverter {
        return UniqueIdentifierToStringConverter()
    }
    @Bean
    @ConditionalOnMissingBean(name = ["partyToStringConverter"])
    fun partyToStringConverter(): PartyToStringConverter {
        return PartyToStringConverter()
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
    /*
    @Bean
    fun mappingJackson2HttpMessageConverter(): MappingJackson2HttpMessageConverter {
        return MappingJackson2HttpMessageConverter().apply {
            this.objectMapper = ObjectMapper().apply {
                registerModule(KotlinModule().apply {
                    setDeserializerModifier(KotlinObjectDeserializerModifier)
                })
            }
        }
    }
*/
    @Bean
    fun addAutowireInjectionToJackson(): Jackson2ObjectMapperBuilderCustomizer {
        return Jackson2ObjectMapperBuilderCustomizer() {
            fun customize(jacksonObjectMapperBuilder: Jackson2ObjectMapperBuilder) {

                jacksonObjectMapperBuilder.handlerInstantiator(
                        SpringHandlerInstantiator(this.applicationContext.autowireCapableBeanFactory))

            }
        }
    }
/*
    @Bean
    fun addCordaJacksonModule(): CordaModule {
        return CordaModule()
    }

 */
    /** Force Spring/Jackson to use a Corda ObjectMapper for (de)serialization
    @Bean
    @Primary
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
        val mapper = if (!cordaNodesProperties.objectMapper.enableRpc
                || proxyServiceKey == null
                || proxyServiceKey.isBlank()) {
            // Warn if possibly missconfigured
            if (cordaNodesProperties.objectMapper.enableRpc) logger.warn(
                    "Using an RPC-based ObjectMapper was enabled but no valid proxyServiceKey was given. " +
                            "Falling back to non-RPC")
            else logger.debug("Creating non-RPC Corda ObjectMapper")
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
    } */

}
