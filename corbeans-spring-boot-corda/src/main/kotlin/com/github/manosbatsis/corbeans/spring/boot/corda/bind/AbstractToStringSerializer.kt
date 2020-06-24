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
import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import java.io.IOException

abstract class AbstractToStringSerializer<T> : JsonSerializer<T?>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun serialize(
            obj: T?, jsonGenerator: JsonGenerator, serializerProvider: SerializerProvider?
    ) {
        if(obj == null){
            jsonGenerator.writeNull()
        }
        else {
            jsonGenerator.writeString(obj.toString())
        }
    }
}
abstract class AbstractFromStringDeserializer<T> : JsonDeserializer<T?>() {
    @Throws(IOException::class, JsonProcessingException::class)
    override fun deserialize(jsonParser: JsonParser,
                             deserializationContext: DeserializationContext?): T? {
        val value: String? = jsonParser.text

        return if(value == null) null else try {
            fromString(value)
        } catch (e: IllegalArgumentException) {
            throw JsonParseException(jsonParser, "Invalid value ${value}: ${e.message}", e)
        }
    }

    abstract fun fromString(value: String): T

}
