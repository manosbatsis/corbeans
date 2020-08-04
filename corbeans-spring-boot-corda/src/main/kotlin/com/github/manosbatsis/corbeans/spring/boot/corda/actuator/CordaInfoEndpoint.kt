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
import com.github.manosbatsis.vaultaire.dto.info.ExtendedNodeInfo
import com.github.manosbatsis.vaultaire.dto.info.NetworkInfo
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.actuate.endpoint.annotation.Endpoint
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation
import org.springframework.boot.actuate.endpoint.annotation.Selector
import java.util.Optional

/** Provides information on known Corda nodes */
@Endpoint(id = "corda")
class CordaInfoEndpoint {

    @Suppress("SpringJavaInjectionPointsAutowiringInspection")
    @Autowired
    lateinit var networkService: CordaNetworkService

    /** Get information on all known nodes */
    @ReadOperation
    fun info(): NetworkInfo {
        return this.networkService.getInfo()
    }

    /** Get information of known node */
    @ReadOperation
    fun node(@Selector name: String): ExtendedNodeInfo {
        return this.networkService.getNodeService(Optional.ofNullable(name)).getExtendedInfo()
    }

}