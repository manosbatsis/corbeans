package com.github.manosbatsis.corda.spring.autoconfigure


import com.github.manosbatsis.corda.spring.beans.CordaNodeServiceImpl
import com.github.manosbatsis.corda.spring.beans.util.SimpleNodeRpcConnection
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

    protected lateinit var cordaNodesProperties: CordaNodesProperties


    companion object {
        private val logger = LoggerFactory.getLogger(NodeServiceBeanFactoryPostProcessor::class.java)
    }

    /**
     * Parse spring-boot config manually since it's not available yet
     */
    override fun setEnvironment(env: Environment) {
        this.cordaNodesProperties = this.buildCordaNodesProperties(env as ConfigurableEnvironment)
    }

    /**
     * Create and register a CordaNodeService bean per node according to configuration
     */
    @Throws(BeansException::class)
    override fun postProcessBeanFactory(beanFactory: ConfigurableListableBeanFactory) {
        val beanDefinitionRegistry = beanFactory as BeanDefinitionRegistry

        this.cordaNodesProperties.nodes.forEach{ (nodeName, nodeParams) ->
            logger.debug("postProcessBeanFactory, nodeName: {}, nodeParams: {}", nodeName, nodeParams)

            // register RPC connection wrapper bean
            val rpcConnectionBeanName = "${nodeName}RpcConnection";
            val rpcConnectionBean = BeanDefinitionBuilder
                    // TODO: make service class configurable
                    .rootBeanDefinition(SimpleNodeRpcConnection::class.java)
                    .setScope(SCOPE_SINGLETON)
                    //.setScope(SCOPE_PROTOTYPE)
                    .addConstructorArgValue(nodeParams)
                    .getBeanDefinition()
            beanDefinitionRegistry.registerBeanDefinition(rpcConnectionBeanName, rpcConnectionBean)
            logger.info("Registered RPC connection bean {} for Party {}", rpcConnectionBeanName, nodeName)

            // verify node service type
            val serviceType = Class.forName(nodeParams.serviceType)
            if(!CordaNodeServiceImpl::class.java.isAssignableFrom(serviceType)){
                throw IllegalArgumentException("Provided service type for node ${nodeName} does not extend CordaNodeServiceImpl")
            }
            // register Node service
            val nodeServiceBeanName = "${nodeName}NodeService";
            val nodeServiceBean = BeanDefinitionBuilder
                    // TODO: make service class configurable
                    .rootBeanDefinition(serviceType)
                    .addConstructorArgReference(rpcConnectionBeanName)
                    .getBeanDefinition()
            beanDefinitionRegistry.registerBeanDefinition(nodeServiceBeanName, nodeServiceBean)
            logger.info("Registered node service {} for Party: {}, type: {}", nodeServiceBeanName, nodeName, serviceType.name)
        }
    }

    /**
     * Parse spring-boot config files to a CordaNodesProperties instance
     */
    fun buildCordaNodesProperties(environment: ConfigurableEnvironment): CordaNodesProperties {
        val sources = ConfigurationPropertySources.from(environment.propertySources)
        val binder = Binder(sources)
        return binder.bind("spring-corda", CordaNodesProperties::class.java).orElseCreate(CordaNodesProperties::class.java)
    }
}
