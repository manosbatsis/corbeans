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

import com.fasterxml.jackson.databind.DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.github.manosbatsis.corbeans.corda.common.NodeDriverNodesConfigWrapper
import com.github.manosbatsis.corda.testacles.nodedriver.config.NodeDriverNodesConfig
import com.github.manosbatsis.corda.testacles.nodedriver.config.SimpleNodeDriverNodesConfig
import java.util.Properties

object Util {


    fun <T> loadProperties(
            resourcePath: String = "/application.properties",
            configClass: Class<T>,
            ignoreErrors: Boolean = false
    ): NodeDriverNodesConfig {
        var cordaNodesProperties: NodeDriverNodesConfig? = null
        try {
            val inputStream = this::class.java.getResourceAsStream(resourcePath)
            val properties = Properties()
            properties.load(inputStream)
            val mapper = com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper()
            mapper.configure(ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            mapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            val tmp: T = mapper
                    .readPropertiesAs(properties, configClass)
            cordaNodesProperties = when(tmp){
                            is NodeDriverNodesConfigWrapper -> tmp.getNodeDriverNodesConfig()
                            is NodeDriverNodesConfig -> tmp
                            else -> throw IllegalArgumentException("Unknown config type: ${configClass.name}")
                        }

            // Fix parsing
            if (cordaNodesProperties.cordapPackages.size == 1) {
                cordaNodesProperties.cordapPackages = cordaNodesProperties.cordapPackages.first()
                        .replace(',', ' ').split(' ')
            }
        } catch (e: Throwable) {
            if (ignoreErrors) e.printStackTrace() else throw e
        }
        return cordaNodesProperties ?: SimpleNodeDriverNodesConfig()

    }
}