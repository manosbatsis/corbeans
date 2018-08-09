package com.github.manosbatsis.corda.spring.autoconfigure


import com.github.manosbatsis.corda.spring.beans.SecureHashConverter
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration


/**
 * Auto-configures a CordaNodeService for each Corda Node
 */
@Configuration
@ComponentScan(basePackages = arrayOf("com.github.manosbatsis.corda.spring"))
open class CordaNodesAutoConfiguration {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesAutoConfiguration::class.java)

        @Bean
        @ConditionalOnMissingBean(NodeServiceBeanFactoryPostProcessor::class)
        @JvmStatic
        fun nodeServiceBeanFactoryPostProcessor(): NodeServiceBeanFactoryPostProcessor {
            return NodeServiceBeanFactoryPostProcessor()
        }
    }

    @Bean
    @ConditionalOnMissingBean(SecureHashConverter::class)
    fun secureHashConverter(): SecureHashConverter {
        return SecureHashConverter()
    }

}
