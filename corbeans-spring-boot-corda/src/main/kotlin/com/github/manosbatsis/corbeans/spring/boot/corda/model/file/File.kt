package com.github.manosbatsis.corbeans.spring.boot.corda.model.file

import net.corda.core.serialization.CordaSerializable
import java.io.InputStream

/** File DTO */
@CordaSerializable
data class File(
        val name: String,
        val originalFilename: String,
        val inputStream: InputStream,
        val size: Long,
        val contentType: String?)