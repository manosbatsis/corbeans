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

import com.fasterxml.jackson.databind.JsonNode
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpStatus


open class InfoIntegrationTests(
        val restTemplate: TestRestTemplate,
        val networkService: CordaNetworkService
) {

    companion object {
        private val logger = LoggerFactory.getLogger(InfoIntegrationTests::class.java)

    }


    @Test
    fun `Can access swagger UI`() {
        // Check swagger endpoint
        var apiDocs = restTemplate
                .getForEntity("/v3/api-docs", JsonNode::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, apiDocs.statusCode)
        // Check Swagger UI
        var swaggerUi = restTemplate.getForEntity("/swagger-ui.html", String::class.java)
        // Ensure a 200 OK
        Assertions.assertEquals(HttpStatus.OK, swaggerUi.statusCode)
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
