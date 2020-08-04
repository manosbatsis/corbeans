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
package com.github.manosbatsis.corbeans.corda.common.test

import com.github.manosbatsis.corbeans.corda.common.CorbeansNodesPropertiesWrapper
import com.github.manosbatsis.corbeans.corda.common.NodesProperties
import com.github.manosbatsis.corbeans.corda.common.Util
import com.github.manosbatsis.partiture.test.MockNodeParametersConfig
import com.github.manosbatsis.partiture.test.MockNodeParametersConfigFlowTest
import com.github.manosbatsis.partiture.test.SimpleMockNodeParametersConfig
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.core.CHARLIE_NAME
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNodeParameters
import net.corda.testing.node.StartedMockNode

open class CorbeansMockNodeParametersConfig(val requireApplicationProperties: Boolean = false) : SimpleMockNodeParametersConfig() {

    companion object {
        val ignoredNodeNames = listOf("default", "cordform")
    }

    open val cordaNodesProperties: NodesProperties by lazy {
        loadCordaNodesProperties()
    }

    protected open fun loadCordaNodesProperties(): NodesProperties =
            Util.loadProperties(CorbeansNodesPropertiesWrapper.Config)

    override fun getCordappPackages(): List<String> = cordaNodesProperties.cordapPackages

    /**
     * Get node parameters for the [StartedMockNode]s to create. Uses an application.properties
     * configuration used to configure Corbeans (you may use a shared test resources module or JAR, or even add to your test resource paths)
     * to create the hints if such a file is fount in the classpath, otherwise returns defaults
     * [ALICE_NAME], [BOB_NAME] and [CHARLIE_NAME] or throws an error based on
     */
    override fun getNodeParameters(): List<MockNodeParameters> {

        return if (this.cordaNodesProperties.nodes.isNotEmpty()) {
            this.cordaNodesProperties.nodes.filterNot { ignoredNodeNames.contains(it.key) }.map {
                val nodeName = it.key
                val testPartyName = it.value.partyName
                val x500Name = if (testPartyName != null) CordaX500Name.parse(testPartyName)
                else CordaX500Name(nodeName, "Athens", "GR")
                MockNodeParameters(legalName = x500Name)
            }
        } else {
            if (requireApplicationProperties) throw IllegalArgumentException("Could not find application.properties in source/classpath and requireApplicationProperties as true")
            super.getNodeParameters()
        }
    }
}

/**
 * Automatically intitializes a [MockNetwork] with [StartedMockNode]s based on the given [MockNodeParametersConfig].
 * Uses a [CorbeansMockNodeParametersConfig] by default.
 */
abstract class CorbeansMockNetworkFlowTest(
        config: MockNodeParametersConfig = CorbeansMockNodeParametersConfig()
) : MockNodeParametersConfigFlowTest(config)
