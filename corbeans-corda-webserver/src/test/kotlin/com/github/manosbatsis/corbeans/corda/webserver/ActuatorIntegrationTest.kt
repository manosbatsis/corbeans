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
package com.github.manosbatsis.corbeans.corda.webserver

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus

/**
 * Actuator integration tests
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
class ActuatorIntegrationTest {

    companion object {
        private val logger = LoggerFactory.getLogger(ActuatorIntegrationTest::class.java)
    }

    // autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can inject services`() {
        assertNotNull(this.networkService)
    }


    @Test
    fun `Can see Corda details within Actuator info endpoint response`() {
        logger.debug("testInfoContributor, called")
        val entity = this.restTemplate
                .getForEntity("/actuator/info", Map::class.java)
        // Ensure a 200 OK
        assertEquals(HttpStatus.OK, entity.statusCode)

        val body = entity.body
        assertNotNull(body, "Actuator info must not be null")
        val corda = body!!["corda"] as Map<*, *>?
        // Validate corda information
        validateCordaInfo(corda)
    }

    @Test
    fun `Can access Corda custom Actuator endpoint`() {
        logger.debug("testCordaEndpoint, called")
        val serviceKeys = this.networkService.nodeServices.keys
        logger.debug("testCordaEndpoint, serviceKeys: {}", serviceKeys)
        val entity = this.restTemplate
                .getForEntity("/actuator/corda", Map::class.java)
        // Ensure a 200 OK
        assertEquals(HttpStatus.OK, entity.statusCode)
        val corda = entity.body
        // Validate corda information
        validateCordaInfo(corda)
    }


    private fun validateCordaInfo(corda: Map<*, *>?) {
        assertNotNull(corda, "Actuator corda info must not be null")
        val cordaNodes = corda!!["nodes"] as Map<String, Any>
        assertNotNull(cordaNodes["partyA"])
        assertNotNull(cordaNodes["partyB"])
    }


}