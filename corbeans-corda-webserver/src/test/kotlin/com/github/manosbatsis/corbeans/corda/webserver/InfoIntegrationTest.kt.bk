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
import com.github.manosbatsis.corbeans.test.integration.WithImplicitNetworkIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status


/**
 * Same as [SingleNetworkIntegrationTest] only using [CorbeansSpringExtension]
 * instead of extending [WithImplicitNetworkIT]
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
@AutoConfigureMockMvc
class InfoIntegrationTest {

    companion object {
        private val logger = LoggerFactory.getLogger(InfoIntegrationTest::class.java)

    }

    // autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can access swagger UI`() {
        // Check swagger endpoint
        this.mockMvc.perform(get("/api-docs"))
                .andExpect(status().isOk())
        // Check Swagger UI
        this.mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().isOk())
    }

    @Test
    fun `Can see Corda details within Actuator info endpoint response`() {
        logger.info("testInfoContributor, called")
        val entity = this.restTemplate
                .getForEntity("/actuator/info", Map::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)

        val body = entity.body
        Assertions.assertNotNull(body, "Actuator info must not be null")
        val corda = body!!["corda"] as Map<*, *>?
        // Validate corda information
        validateCordaInfo(corda)
    }

    @Test
    fun `Can access Corda custom Actuator endpoint`() {
        logger.info("testCordaEndpoint, called")
        val serviceKeys = this.networkService.nodeServices.keys
        logger.info("testCordaEndpoint, serviceKeys: {}", serviceKeys)
        val entity = this.restTemplate
                .getForEntity("/actuator/corda", Map::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, entity.statusCode)
        val corda = entity.body
        // Validate corda information
        validateCordaInfo(corda)
    }


    private fun validateCordaInfo(corda: Map<*, *>?) {
        Assertions.assertNotNull(corda, "Actuator corda info must not be null")
        val cordaNodes = corda!!["nodes"] as Map<String, Any>
        Assertions.assertNotNull(cordaNodes["partyA"])
        Assertions.assertNotNull(cordaNodes["partyB"])
    }


}
