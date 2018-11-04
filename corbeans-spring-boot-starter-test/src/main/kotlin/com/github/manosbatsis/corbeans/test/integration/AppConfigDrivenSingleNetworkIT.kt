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
package com.github.manosbatsis.corda.webserver.spring

import com.github.manosbatsis.corbeans.test.integration.BaseAppConfigDrivenNetworkIT
import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory

/**
 * Automatically creates and maintains a single Corda network throughout test execution using `withDriverNodes`
 * and the application's corbeans (auto)configuration from application.properties
 *
 * Using a single driver instance and network improves performance VS multiple `withDriverNodes` per test suite
 * and allows to simply autowire components registered by corbeans auto-configuration, for example:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * @ExtendWith(SpringExtension::class)
 * class MySampleIntegrationTest : AppConfigDrivenSingleNetworkIT() {
 *
 *      // tell the driver which cordapp packages to load
 *      override fun getCordappPackages(): List<String> = listOf("net.corda.finance")
 *
 *      // autowire a service for a specific node
 *      @Autowired
 *      @Qualifier("partyANodeService")
 *      lateinit var service: CordaNodeService
 *
 *      // autowire a unique-typed custom node service
 *      @Autowired
 *      lateinit var customCervice: SampleCustomCordaNodeServiceImpl
 *
 *      @Test
 *      fun `Can inject services`() {
 *          assertNotNull(this.service)
 *          assertNotNull(this.customCervice)
 *      }
 *
 *      @Test
 *      fun `Can retrieve node identity`() {
 *          assertNotNull(service.myIdentity)
 *      }
 * }
 * ```
 *
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AppConfigDrivenSingleNetworkIT: BaseAppConfigDrivenNetworkIT() {

    companion object {
        private val logger = LoggerFactory.getLogger(AppConfigDrivenSingleNetworkIT::class.java)
    }

    /**
     * Starts the Corda network
     */
    @BeforeAll
    open fun startNetwork() {
        startNetworkAsync()
        // wait for startup completion
        while (!started) {
            Thread.sleep(1000)
        }
    }

    /**
     * Stops the Corda network
     */
    @AfterAll
    open fun stopNetwork() {
        try {
            finished = true
            // give time for a clean shutdown
            while (!stopped) {
                Thread.sleep(1000)
            }
        } catch (e: Exception) {
            logger.error("stopNetwork failed:", e)
            throw e
        }
    }

    /**
     * Starts and maintains a network running in parallel with tests
     */
    @Suppress("EXPERIMENTAL_FEATURE_WARNING")
    private fun startNetworkAsync() = GlobalScope.async {
        super.withDriverNodes{
            while (!finished) {
                Thread.sleep(1000)
            }
        }

    }

    /**
     * Don't allow concurrent copies of the same network
     */
    override fun withDriverNodes(action: () -> Unit) {
        throw IllegalStateException(
                "You can only use the single Corda network context running in parallel with this implementation")
    }

}