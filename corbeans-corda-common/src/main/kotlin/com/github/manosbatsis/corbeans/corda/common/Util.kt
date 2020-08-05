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
import java.util.Properties

object Util {


    fun loadProperties(
            config: NodesPropertiesLoadingConfig
    ): NodesProperties {
        var cordaNodesProperties: NodesProperties? = null
        try {
            val inputStream = this::class.java.getResourceAsStream(config.resourcePath)
            val properties = Properties()
            properties.load(inputStream)
            val mapper = JavaPropsMapper()
            mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            cordaNodesProperties = mapper
                    .readPropertiesAs(properties, config.wrappingClass).toNodesProperties()
            // Fix parsing
            if (cordaNodesProperties.cordapPackages.size == 1) {
                cordaNodesProperties.cordapPackages = cordaNodesProperties.cordapPackages.first()
                        .replace(',', ' ').split(' ')
            }
        } catch (e: Throwable) {
            if (config.ignoreError) e.printStackTrace() else throw e
        }
        return cordaNodesProperties ?: NodesProperties()

    }
}

interface NodesPropertiesLoadingConfig {
    companion object {
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
) : NodesPropertiesLoadingConfig
