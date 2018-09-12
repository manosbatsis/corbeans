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

import kotlinx.coroutines.experimental.GlobalScope
import kotlinx.coroutines.experimental.async
import org.slf4j.LoggerFactory

/**
 * Used a single Corda driver to maintain a single networkContext throughout test execution.
 * Uses the application's (auto)configuration from application.properties
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class AbstractSingleCordaNetworkIntegrationTest: AbstractCordaNetworkCapableIntegrationTest() {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractSingleCordaNetworkIntegrationTest::class.java)
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
    private fun startNetworkAsync() = GlobalScope.async {
        super.networkContext{
            while (!finished) {
                Thread.sleep(1000)
            }
        }

    }

    /**
     * Don't allow concurrent copies of the same network
     */
    override fun networkContext(action: () -> Unit) {
        throw NotImplementedError("You can only use the single network context running in parallel with this implementation")
    }

}