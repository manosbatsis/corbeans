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
    lateinit var cordaNodesProperties: CordaNodesProperties

    /**
     * Delegate to [SpringExtension.beforeAll],
     * then start the Corda network
     */
    @Throws(Exception::class)
    override fun beforeAll(context: ExtensionContext) {
        // Delegate to super
        super.beforeAll(context)
        // Get the nodes config from spring's application context
        this.cordaNodesProperties = SpringExtension.getApplicationContext(context)
                .getBean(CordaNodesProperties::class.java)
        logger.debug("beforeAll for nodes: {}", this.cordaNodesProperties.nodes.keys)
        // Start the network
        this.nodeDriverHelper = NodeDriverHelper(this.cordaNodesProperties)
        this.nodeDriverHelper.startNetwork()
    }

    /**
     * Delegate to [SpringExtension.afterAll],
     * then stop the Corda network
     */
    @Throws(Exception::class)
    override fun afterAll(context: ExtensionContext) {
        super.afterAll(context)
        logger.debug("afterAll for nodes: {}", this.cordaNodesProperties.nodes.keys)
        this.nodeDriverHelper.stopNetwork()
    }
}