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
package com.github.manosbatsis.corbeans.test.integration

import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * Provides Corda (driver) network per [withDriverNodes] call, using the corbeans'
 * config from `application.properties`. You may override the latter with an
 * additional file in your test classpath, i.e. `src/test/resources/application.properties`.
 *
 * Sample:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * @ExtendWith(SpringExtension::class)
 * class MyWithDriverNodesIntegrationTest : WithDriverNodesIT() {
 *
 *      @Test
 *      fun `Can create services`() {
 *          withDriverNodes {
 *              assertNotNull(this.services)
 *              assertTrue(this.services.keys.isNotEmpty())
 *          }
 *      }
 *
 *      @Test
 *      fun `Can retrieve node identity`() {
 *          withDriverNodes {
 *              assertNotNull(services["partyANodeService"]?.myIdentity)
 *          }
 *      }
 *
 *      @Test
 *      fun `Can retrieve notaries`() {
 *          withDriverNodes {
 *              assertNotNull(services["partyANodeService"]?.notaries())
 *          }
 *      }
 * }
 * ```
 */
abstract class WithDriverNodesIT {

    companion object {
        private val logger = LoggerFactory.getLogger(WithDriverNodesIT::class.java)
    }

    @Autowired
    lateinit var cordaNodesProperties: CordaNodesProperties

    /**
     * Launch a network, execute the action code, and shut the network down
     */
    open fun withDriverNodes(action: () -> Unit) {
        NodeDriverHelper(cordaNodesProperties).withDriverNodes(action)
    }

}
