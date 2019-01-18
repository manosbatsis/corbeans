package com.github.manosbatsis.corbeans.spring.boot.corda.model.file

import com.fasterxml.jackson.annotation.JsonFormat
import net.corda.core.serialization.CordaSerializable
import java.time.LocalDateTime

/**
 * Receipt of an attachment saved to the vault
 */
@CordaSerializable
data class FileEntry(
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy, HH:mm:ss", timezone = "UTC")
        val date: LocalDateTime,
        val hash: String,
        val name: String,
        val author: String,
        val version: String?)