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
package com.github.manosbatsis.corbeans.corda.common

import com.github.manosbatsis.corda.rpc.poolboy.config.NodeParams
import com.github.manosbatsis.corda.rpc.poolboy.config.PoolParams


interface NodesPropertiesWrapper {
    fun toNodesProperties(): NodesProperties
}

/**
 * Used to wrap a [NodesProperties] for easier parsing via jackson-dataformats-text,
 * as a temporary workaround to https://github.com/FasterXML/jackson-dataformats-text/issues/100
 */
class CorbeansNodesPropertiesWrapper : NodesPropertiesWrapper {
    companion object Config : NodesPropertiesLoadingConfig {
        override val resourcePath = "/application.properties"
        override val wrappingClass = CorbeansNodesPropertiesWrapper::class.java
        override val ignoreError = true
    }

    var corbeans: NodesProperties? = null
    override fun toNodesProperties() = corbeans!!
}


open class NodesProperties : NodesPropertiesWrapper {
    var cordapPackages: List<String> = mutableListOf()
    var nodes: Map<String, NodeParams> = mutableMapOf()
    var bnmsServiceType: String? = null
    var notarySpec: TestNotaryProperties = TestNotaryProperties()
    var flowOverrides: List<String> = mutableListOf()
    var poolParams: PoolParams = PoolParams()
    override fun toNodesProperties(): NodesProperties = this

    override fun toString(): String {
        return "NodesProperties(cordapPackages=$cordapPackages, " +
                "nodes=$nodes, " +
                "notarySpec=$notarySpec, " +
                "flowOverrides=${flowOverrides}), " +
                "bnmsServiceType=${bnmsServiceType}, " +
                "poolParams=${poolParams}"
    }
}


