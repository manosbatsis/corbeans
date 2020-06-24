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

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import net.corda.core.identity.Party
import org.springframework.boot.jackson.JsonComponent
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for transparent String<>SecureHash binding
 */
class PartyToStringConverter : Converter<Party, String?> {
    override fun convert(source: Party): String? {
        return source.name.toString()
    }
}
@JsonComponent
class PartyConverter : AbstractToStringSerializer<Party>(){
    override fun serialize(
            obj: Party?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?
    ) {
        if(obj == null){
            jsonGenerator.writeNull()
        }
        else {
            jsonGenerator.writeString(obj.name.toString())
        }
    }
}
