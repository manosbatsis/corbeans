package com.github.manosbatsis.corbeans.spring.boot.corda.actuator

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor

/** Provides additional info to actuator's info endpoint based on known Corda nodes */
class CordaInfoContributor : InfoContributor {

    @Autowired
    lateinit var networkService: CordaNetworkService

    /** Extend info based on known nodes etc. */
    override fun contribute(builder: Info.Builder) {
        builder.withDetail("corda", networkService.getInfo())
    }
}