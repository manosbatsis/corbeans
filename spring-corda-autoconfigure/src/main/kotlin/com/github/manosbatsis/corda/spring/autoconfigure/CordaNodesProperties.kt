package com.github.manosbatsis.corda.spring.autoconfigure

import com.github.manosbatsis.corda.spring.beans.util.NodeParams
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component


@Component
@ConfigurationProperties(prefix = "spring-corda")
open class CordaNodesProperties {

    open lateinit var nodes: Map<String, NodeParams>


}
