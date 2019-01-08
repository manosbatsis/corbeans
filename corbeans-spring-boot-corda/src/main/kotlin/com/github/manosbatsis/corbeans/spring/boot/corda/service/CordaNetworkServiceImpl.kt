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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

//import org.springframework.messaging.simp.SimpMessagingTemplate
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired


/**
 * Default Corda network service
 */
open class CordaNetworkServiceImpl: CordaNetworkService {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNetworkServiceImpl::class.java)
    }

    /** Services per node, mapped by configured name */
    @Autowired
    override lateinit var services: Map<String, CordaNodeService>

}