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
package com.github.manosbatsis.corbeans.spring.boot.corda.model.upload

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonFormat
import net.corda.core.serialization.CordaSerializable
import java.util.Date

/**
 * Receipt of an attachment saved to the vault.
 * Annotated with [CordaSerializable] and thus [ContractState]-embeddable.
 */
@CordaSerializable
class AttachmentReceipt @JsonCreator constructor(
        /** The datetime of attachment persistence to the vault */
        @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd/MM/yyyy, HH:mm:ss", timezone = "UTC")
        var date: Date? = null,
        /** The attachment [net.corda.core.crypto.SecureHash] string */
        var hash: String? = null,
        /** The attached files */
        var files: List<String> = emptyList(),
        /** The attachment authoring organization */
        var author: String? = null,
        /** Whether an original archive was persisted in the vault, `false` if automatically created  */
        var savedOriginal: Boolean = false
)
