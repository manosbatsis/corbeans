package com.github.manosbatsis.corda.spring.beans

import net.corda.core.crypto.SecureHash
import org.springframework.core.convert.converter.Converter

/**
 * Custom converter for transparent String<>SecureHash binding
 */
class SecureHashConverter : Converter<String, SecureHash> {

    override fun convert(source: String): SecureHash {
        return SecureHash.parse(source)
    }


}