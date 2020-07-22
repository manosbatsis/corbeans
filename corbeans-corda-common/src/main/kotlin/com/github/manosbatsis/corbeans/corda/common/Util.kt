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


    fun loadProperties(
            config: NodesPropertiesLoadingConfig
    ): NodesProperties{
        var cordaNodesProperties: NodesProperties? = null
        try {
            val inputStream = this::class.java.getResourceAsStream(config.resourcePath)
            val properties = Properties()
            properties.load(inputStream)
            val mapper = JavaPropsMapper()
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            cordaNodesProperties = mapper
                    .readPropertiesAs(properties, config.wrappingClass).toNodesProperties()!!
            // Fix parsing
            if (cordaNodesProperties.cordapPackages.size == 1) {
                cordaNodesProperties.cordapPackages = cordaNodesProperties.cordapPackages.first()
                        .replace(',', ' ').split(' ')
            }
        }
        catch (e: Throwable){
            if(config.ignoreError) e.printStackTrace() else throw e
        }
        return cordaNodesProperties ?: NodesProperties()

    }
}

interface NodesPropertiesLoadingConfig{
    companion object{
        fun create(
                resourcePath: String,
                wrappingClass: Class<out NodesPropertiesWrapper> =
                    NodesProperties::class.java,
                ignoreError: Boolean = false
        ) = NodesPropertiesLoadingConfigData(
                resourcePath, wrappingClass, ignoreError)
    }
    val resourcePath: String
    val wrappingClass: Class<out NodesPropertiesWrapper>
    val ignoreError: Boolean
}
class NodesPropertiesLoadingConfigData(
        override val resourcePath: String,
        override val wrappingClass: Class<out NodesPropertiesWrapper>,
        override val ignoreError: Boolean
): NodesPropertiesLoadingConfig

open class NodesProperties: NodesPropertiesWrapper {
    var cordapPackages: List<String> = mutableListOf()
    var nodes: Map<String, NodeParams> = mutableMapOf()
    var objectMapper: ObjectMapperProperties = ObjectMapperProperties()
    var primaryControllerType: String? = "com.github.manosbatsis.corbeans.spring.boot.corda.web.CordaSingleNodeController"
    var notarySpec: TestNotaryProperties = TestNotaryProperties()
    var flowOverrides: List<String> = mutableListOf()
    override fun toNodesProperties(): NodesProperties = this

    override fun toString(): String {
        return "NodesProperties(cordapPackages=$cordapPackages, " +
                "nodes=$nodes, " +
                "notarySpec=$notarySpec, " +
                "flowOverrides=${flowOverrides}), " +
                "primaryControllerType=${primaryControllerType}, " +
                "objectMapper=${objectMapper}"
    }

}


interface NodesPropertiesWrapper{
    fun toNodesProperties(): NodesProperties
}

/**
 * Used to wrap a [NodesProperties] for easier parsing via jackson-dataformats-text,
 * as a temporary workaround to https://github.com/FasterXML/jackson-dataformats-text/issues/100
 */
class CorbeansNodesPropertiesWrapper: NodesPropertiesWrapper{
    companion object Config: NodesPropertiesLoadingConfig{
        override val resourcePath = "/application.properties"
        override val wrappingClass = CorbeansNodesPropertiesWrapper::class.java
        override val ignoreError = true
    }

    var corbeans: NodesProperties? = null
    override fun toNodesProperties() = corbeans!!
}


