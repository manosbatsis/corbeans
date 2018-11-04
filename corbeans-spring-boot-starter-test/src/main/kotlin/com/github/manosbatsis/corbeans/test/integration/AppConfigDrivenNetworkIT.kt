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
package com.github.manosbatsis.corbeans.test.integration

import com.github.manosbatsis.corbeans.spring.boot.corda.BaseCordaNodeService
import com.github.manosbatsis.corbeans.spring.boot.corda.BaseCordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeService
import com.github.manosbatsis.corbeans.spring.boot.corda.util.LazyNodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 *
 * Provides Corda (driver) network and corbeans components per [withDriverNodes] call.
 * Uses the application's corbeans (auto)configuration from application.properties
 * Sample:
 *
 * ```
 *
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * @ExtendWith(SpringExtension::class)
 * class MySampleIntegrationTest : AppConfigDrivenNetworkIT() {
 *
 *      // tell the driver which cordapp packages to load
 *      override fun getCordappPackages(): List<String> = listOf("net.corda.finance")
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
abstract class AppConfigDrivenNetworkIT: BaseAppConfigDrivenNetworkIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(AppConfigDrivenNetworkIT::class.java)
    }

    /** Maintains a map of all created services, mapped by name */
    @Autowired
    lateinit var services: MutableMap<String, BaseCordaNodeService>

}