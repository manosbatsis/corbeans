package com.github.manosbatsis.corbeans.spring.boot.corda.actuator

import com.github.manosbatsis.corbeans.spring.boot.corda.model.info.NetworkInfo
import com.github.manosbatsis.corbeans.spring.boot.corda.model.info.NodeInfo
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import java.util.*

/** Provides information on known Corda nodes */
@Endpoint(id = "corda")
class CordaInfoEndpoint {

    @Autowired
    lateinit var networkService: CordaNetworkService

    /** Get information on all known nodes */
    @ReadOperation
    fun info(): NetworkInfo {
        return this.networkService.getInfo()
    }

    /** Get information of known node */
    @ReadOperation
    fun node(@Selector name: String): NodeInfo {
        return this.networkService.getNodeService(Optional.ofNullable(name)).getInfo()
    }

}