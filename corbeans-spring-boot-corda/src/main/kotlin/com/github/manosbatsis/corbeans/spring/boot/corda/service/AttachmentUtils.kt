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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

import com.github.manosbatsis.vaultaire.dto.attachment.Attachment
import com.github.manosbatsis.vaultaire.dto.attachment.AttachmentFile
import org.apache.commons.compress.archivers.ArchiveStreamFactory
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.springframework.web.multipart.MultipartFile
import java.io.File


val TMP_FILE_PREFIX: String = "corbeans-uploaded-attachment-"
val TMP_FILE_SUFFIX: String = "-tmp.zip"
val ACCEPTED_TYPES = arrayOf("application/zip", "application/java-archive")

/** Converts [MultipartFile]`s to [AttachmentFile]`s */
fun toAttachmentFiles(uploadedFiles: Array<MultipartFile>): List<AttachmentFile> =
        uploadedFiles.map {
            AttachmentFile(
                    name = it.name,
                    originalFilename = it.originalFilename ?: "unknown-filename",
                    inputStream = it.inputStream,
                    size = it.size,
                    contentType = it.contentType
            )
        }

/** Converts the input [MultipartFile] to an [Attachment] */
fun toAttachment(uploadedFile: MultipartFile): Attachment =
        toAttachment(toAttachmentFiles(arrayOf(uploadedFile)))

/** Converts the input [MultipartFile]s to an [Attachment] */
fun toAttachment(uploadedFiles: Array<MultipartFile>): Attachment =
        toAttachment(toAttachmentFiles(uploadedFiles))

/**
 * Converts the input [AttachmentFile]s to an [Attachment].
 * Uses the original pre-archived file if only such a single file is given.
 */
fun toAttachment(attachmentFiles: List<AttachmentFile>): Attachment {
    // Use the original file if single and already an acceptable archive
    return if (attachmentFiles.size == 1
            && ACCEPTED_TYPES.contains(attachmentFiles.first().contentType)) {
        val archive = attachmentFiles.first()
        Attachment(null, archive.inputStream, listOf(archive.originalFilename), true)
    }
    // Ensure we don't have an empty file list
    else if (attachmentFiles.isEmpty()) {
        throw IllegalArgumentException("Cannot create an empty attachment")
    }
    // Else make a new archive of input files
    else {
        val archive = File.createTempFile(TMP_FILE_PREFIX, TMP_FILE_SUFFIX)
        val filenames = mutableListOf<String>()
        ArchiveStreamFactory()
                .createArchiveOutputStream(ArchiveStreamFactory.ZIP, archive.outputStream())
                .use { zipOs ->
                    // Add archive entries
                    attachmentFiles.mapTo(filenames) { file ->
                        zipOs.putArchiveEntry(ZipArchiveEntry(file.originalFilename))
                        file.inputStream.use { it.copyTo(zipOs) }
                        zipOs.closeArchiveEntry()
                        file.originalFilename
                    }
                    zipOs.finish()
                }
        Attachment(archive, archive.inputStream(), filenames)
    }
}
