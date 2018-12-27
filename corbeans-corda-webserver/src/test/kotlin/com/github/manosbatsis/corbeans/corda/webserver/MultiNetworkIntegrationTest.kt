/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
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

import com.github.manosbatsis.corbeans.corda.webserver.components.SampleCustomCordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeService
import com.github.manosbatsis.corbeans.test.integration.WithDriverNodesIT
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MultiNetworkIntegrationTest : WithDriverNodesIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(MultiNetworkIntegrationTest::class.java)

    }

    override fun getCordappPackages(): List<String> = listOf("net.corda.finance")

    // autowire all created services, mapped by name
    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    // autowire a services for a specific node
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService

    // autowire a unique custom service
    @Autowired
    lateinit var customCervice: SampleCustomCordaNodeServiceImpl

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Test
    fun `Can use both default node and multiple node controller endpoints`() {
        withDriverNodes {
            //val defaultNodeMe = this.restTemplate.getForObject("/node/me", Map::class.java)
            //assertEquals("me", defaultNodeMe.keys.first())

            val defaultNodeMe = this.restTemplate.getForObject("/node/me", Map::class.java)
            Assertions.assertEquals("me", defaultNodeMe.keys.first())
            //val defaultNodeMe = this.restTemplate.getForObject("/node/me", Map::class.java)
            //assertEquals("me", defaultNodeMe.keys.first())

            val partyANodeMe = this.restTemplate.getForObject("/nodes/partyA/me", Map::class.java)
            Assertions.assertEquals("me", partyANodeMe.keys.first())
        }
    }

    @Test
    fun `Can inject services`() {
        withDriverNodes {
            logger.info("services: {}", services)
            assertNotNull(this.services)
            assertNotNull(this.service)
            assertTrue(this.services.keys.isNotEmpty())
        }
    }

    @Test
    fun `Can inject custom service`() {
        withDriverNodes {
            logger.info("customCervice: {}", customCervice)
            assertNotNull(this.customCervice)
            assertTrue(this.customCervice.dummy())
        }
    }

    @Test
    fun `Can retrieve node identity`() {
        withDriverNodes {
            assertNotNull(service.myIdentity)
        }
    }

    @Test
    fun `Can retrieve notaries`() {
        withDriverNodes {
            val notaries: List<Party> = service.notaries()
            assertNotNull(notaries)
        }
    }

    @Test
    fun `Can retrieve flows`() {
        withDriverNodes {
            val flows: List<String> = service.flows()
            assertNotNull(flows)
        }
    }

    @Test
    fun `Can retrieve addresses`() {
        withDriverNodes {
            val addresses: List<NetworkHostAndPort> = service.addresses()
            assertNotNull(addresses)
        }
    }

}