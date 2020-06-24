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
package com.github.manosbatsis.corbeans.spring.boot.corda.bind

import net.corda.core.identity.CordaX500Name
import org.springframework.boot.jackson.JsonComponent
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for transparent String<>CordaX500Name binding
 */
class StringToCordaX500NameConverter : Converter<String, CordaX500Name> {

    override fun convert(source: String): CordaX500Name {
        return CordaX500Name.parse(source)
    }
}
/**
 * Custom converter for transparent CordaX500Name<>String binding
 */
class CordaX500NameToStringConverter : Converter<CordaX500Name, String> {
    override fun convert(source: CordaX500Name):  String{
        return source.toString()
    }
}

@JsonComponent
class CordaX500NameJsonSerializer : AbstractToStringSerializer<CordaX500Name>()

@JsonComponent
class CordaX500NameJsonDeserializer : AbstractFromStringDeserializer<CordaX500Name>(){
    override fun fromString(value: String): CordaX500Name {
        return CordaX500Name.parse(value)
    }
}
