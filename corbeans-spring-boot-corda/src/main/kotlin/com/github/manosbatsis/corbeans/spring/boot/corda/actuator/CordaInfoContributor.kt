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
package com.github.manosbatsis.corbeans.spring.boot.corda.actuator

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import net.corda.client.rpc.RPCException
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor

/** Provides additional info to actuator's info endpoint based on known Corda nodes */
class CordaInfoContributor : InfoContributor {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaInfoContributor::class.java)
    }

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var networkService: CordaNetworkService

    /** Extend info based on known nodes etc. */
    override fun contribute(builder: Info.Builder) {
        try {
            builder.withDetail("corda", networkService.getInfo())
        } catch (e: RPCException) {
            logger.warn("RPC Error contributing to Actuator \"info\" endpoint", e)
        } catch (e: Exception) {
            logger.warn("Error contributing to Actuator \"info\" endpoint", e)
        }
    }
}
