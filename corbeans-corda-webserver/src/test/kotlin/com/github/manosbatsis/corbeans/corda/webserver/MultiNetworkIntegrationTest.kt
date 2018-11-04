/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
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
import com.github.manosbatsis.corbeans.test.integration.AppConfigDrivenNetworkIT
import com.github.manosbatsis.corda.webserver.spring.AppConfigDrivenSingleNetworkIT
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MultiNetworkIntegrationTest : AppConfigDrivenNetworkIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(MultiNetworkIntegrationTest::class.java)

    }

    override fun getCordappPackages(): List<String> = listOf("net.corda.finance")

    @Test
    fun `Can create services`() {
        withDriverNodes {
            logger.info("services: {}", services)
            assertNotNull(this.services)
            assertTrue(this.services.keys.isNotEmpty())
        }
    }


    @Test
    fun `Can retrieve node identity`() {
        withDriverNodes {
            assertNotNull(services["partyANodeService"]?.myIdentity)
        }
    }

    @Test
    fun `Can retrieve notaries`() {
        withDriverNodes {
            assertNotNull(services["partyANodeService"]?.notaries())
        }
    }

}