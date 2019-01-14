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
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired

/**
 * Alternative of extending a base class you VS using [CorbeansSpringExtension].
 *
 * Automatically creates and maintains a single Corda network throughout test execution,
 * using the corbeans' config from `application.properties`. You may override the latter with an
 * additional file in your test classpath, i.e. `src/test/resources/application.properties`.
 *
 * Example:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * @ExtendWith(SpringExtension::class)
 * class MyWithSingleNetworkIntegrationTest : WithImplicitNetworkIT() {
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

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class WithImplicitNetworkIT {

    companion object {
        private val logger = LoggerFactory.getLogger(WithImplicitNetworkIT::class.java)
    }

    @Autowired
    lateinit var cordaNodesProperties: CordaNodesProperties

    lateinit var nodeDriverHelper: NodeDriverHelper

    /**
     * Starts the Corda network
     */
    @BeforeAll
    open fun startNetwork() {
        logger.debug("startNetwork for nodes: {}", cordaNodesProperties.nodes.keys)
        this.nodeDriverHelper = NodeDriverHelper(cordaNodesProperties)
        this.nodeDriverHelper.startNetwork()
    }

    /**
     * Stops the Corda network
     */
    @AfterAll
    open fun stopNetwork() {
        logger.debug("stopNetwork for nodes: {}", cordaNodesProperties.nodes.keys)
        this.nodeDriverHelper.stopNetwork()
    }

}