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

import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 *
 * Alternative JUnit extension VS subclassing [WithImplicitNetworkIT]. To be used instead of [SpringExtension]
 * Automatically creates and maintains a single Corda network throughout test execution,
 * using the corbeans' config from `application.properties`. You may override the latter with an
 * additional file in your test classpath, i.e. `src/test/resources/application.properties`.
 *
 * Example:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * // Use CorbeansSpringExtension instead of SpringExtension
 * @ExtendWith(CorbeansSpringExtension::class)
 * class MyWithSingleNetworkIntegrationTest {
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
 */
class CorbeansSpringExtension: SpringExtension() {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansSpringExtension::class.java)
    }

    lateinit var nodeDriverHelper: NodeDriverHelper

    /**
     * Delegate to [SpringExtension.beforeAll],
     * then start the Corda network
     */
    @Throws(Exception::class)
    override fun beforeAll(context: ExtensionContext) {
        logger.debug("Starting network")
        // Start the network
        this.nodeDriverHelper = NodeDriverHelper()
        this.nodeDriverHelper.startNetwork()
        logger.debug("Network is up, starting container")
        // Nodes are started so we can go on with Spring
        super.beforeAll(context)
    }

    /**
     * Delegate to [SpringExtension.afterAll],
     * then stop the Corda network
     */
    @Throws(Exception::class)
    override fun afterAll(context: ExtensionContext) {
        // Stop Spring first
        logger.debug("Stopping container...")
        super.afterAll(context)
        // Spring stopped, shutdown the network
        logger.debug("Stopping network...")
        this.nodeDriverHelper.stopNetwork()
    }
}
