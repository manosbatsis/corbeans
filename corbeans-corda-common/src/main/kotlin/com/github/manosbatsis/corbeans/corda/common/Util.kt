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

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper
import java.util.*

object Util {

    fun loadProperties(ignoreError: Boolean = false): NodesProperties {
        var cordaNodesProperties: NodesProperties? = null
        try {
            val inputStream = this::class.java.getResourceAsStream("/application.properties")
            val properties = Properties()
            properties.load(inputStream)
            val mapper = JavaPropsMapper()
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            cordaNodesProperties = mapper
                    .readPropertiesAs(properties, NodesPropertiesWrapper::class.java).corbeans!!
            // Fix parsing
            if (cordaNodesProperties.cordapPackages.size == 1) {
                cordaNodesProperties.cordapPackages = cordaNodesProperties.cordapPackages.first()
                        .replace(',', ' ').split(' ')
            }
        }
        catch (e: Throwable){
            if(ignoreError) e.printStackTrace() else throw e
        }
        return cordaNodesProperties ?: NodesProperties()

    }
}

open class NodesProperties {
    var cordapPackages: List<String> = mutableListOf()
    var nodes: Map<String, NodeParams> = mutableMapOf()
    var objectMapper: ObjectMapperProperties = ObjectMapperProperties()
    var primaryControllerType: String? = "com.github.manosbatsis.corbeans.spring.boot.corda.web.CordaSingleNodeController"
    var notarySpec: TestNotaryProperties = TestNotaryProperties()
    var flowOverrides: List<String> = mutableListOf()

    override fun toString(): String {
        return "NodesProperties(cordapPackages=$cordapPackages, " +
                "nodes=$nodes, " +
                "notarySpec=$notarySpec, " +
                "flowOverrides=${flowOverrides}), " +
                "primaryControllerType=${primaryControllerType}, " +
                "objectMapper=${objectMapper}"
    }


}
/**
 * Used to wrap a [NodesProperties] for easier parsing via jackson-dataformats-text,
 * as a temporary workaround to https://github.com/FasterXML/jackson-dataformats-text/issues/100
 */
class NodesPropertiesWrapper {
    var corbeans: NodesProperties? = null
}
