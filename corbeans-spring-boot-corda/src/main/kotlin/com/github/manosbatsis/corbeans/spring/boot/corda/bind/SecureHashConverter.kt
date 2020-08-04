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

import net.corda.core.crypto.SecureHash
import org.springframework.boot.jackson.JsonComponent
import org.springframework.core.convert.converter.Converter


/**
 * Custom converter for transparent String<>SecureHash binding
 */
class StringToSecureHashConverter : Converter<String, SecureHash> {

    override fun convert(source: String): SecureHash {
        return SecureHash.parse(source)
    }
}

/**
 * Custom converter for transparent String<>SecureHash binding
 */
class SecureHashToStringConverter : Converter<SecureHash, String> {

    override fun convert(source: SecureHash): String {
        return source.toString()
    }
}

@JsonComponent
class SecureHashJsonSerializer : AbstractToStringSerializer<SecureHash>()

@JsonComponent
class SecureHashJsonDeserializer : AbstractFromStringDeserializer<SecureHash>() {
    override fun fromString(value: String): SecureHash {
        return SecureHash.parse(value)
    }
}
