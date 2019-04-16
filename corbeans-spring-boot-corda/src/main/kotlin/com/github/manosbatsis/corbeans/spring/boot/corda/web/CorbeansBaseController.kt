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
package com.github.manosbatsis.corbeans.spring.boot.corda.web

import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentReceipt
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.toAttachment
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.internal.extractFile
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.time.LocalDateTime
import java.util.*
import java.util.jar.JarInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 *  Base controller implementation with basic utility for Corda nodes. Supports multiple nodes by
 *  using an optional <code>nodeName</code> method parameter to construct a Service Bean name,
 *  then uses it as a key to lookup and obtain  a CordaNodeService for the specific node
 *  from the autowired services map.
 */
abstract class CorbeansBaseController {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansBaseController::class.java)
    }

    @Autowired
    protected lateinit var networkService: CordaNetworkService

    /** Get the node's X500 principal name. */
    open fun me(nodeName: Optional<String>) =
            mapOf("me" to getNodeService(nodeName).myIdentity.name.x500Principal.name.toString())

    /** Get the node's identity name. */
    open fun whoami(nodeName: Optional<String>) =
            mapOf("me" to getNodeService(nodeName).myIdentity.name)


    /** Get a list of nodes in the network.*/
    open fun nodes(nodeName: Optional<String>) = getNodeService(nodeName).nodes()

    /** Get a list of the node's network peers.*/
    open fun peers(nodeName: Optional<String>) = getNodeService(nodeName).peers()

    /** Get a list of the node's network peer names. */
    open fun peerNames(nodeName: Optional<String>) = getNodeService(nodeName).peerNames()

    /** Get tbe node time in UTC */
    open fun serverTime(nodeName: Optional<String>): LocalDateTime {
        return getNodeService(nodeName).serverTime()
    }

    /** Get tbe node addresses */
    open fun addresses(nodeName: Optional<String>): List<NetworkHostAndPort> {
        return getNodeService(nodeName).addresses()
    }

    /** Get tbe node identities */
    open fun identities(nodeName: Optional<String>): List<Party> {
        return getNodeService(nodeName).identities()
    }

    /** Get tbe node's platform version */
    open fun platformVersion(nodeName: Optional<String>): Int {
        return getNodeService(nodeName).platformVersion()
    }

    /** Get tbe node flows */
    open fun flows(nodeName: Optional<String>): List<String> {
        return getNodeService(nodeName).flows()
    }

    /** Get tbe node notaries */
    open fun notaries(nodeName: Optional<String>): List<Party> {
        return getNodeService(nodeName).notaries()
    }

    /** Get tbe node states */
    open fun states(nodeName: Optional<String>): List<StateAndRef<ContractState>> {
        return getNodeService(nodeName).states()
    }

    /** List the contents of the attachment archive matching the given hash */
    open fun listAttachmentFiles(nodeName: Optional<String>,
                                 hash: SecureHash): List<String> {
        val entries = mutableListOf<String>()
        getNodeService(nodeName).openAttachment(hash).use { attachmentArchive ->
            ZipInputStream(attachmentArchive).use {
                var entry: ZipEntry? = it.nextEntry
                while (entry != null) {
                    entries += entry.name
                    entry = it.nextEntry
                }
            }
        }
        return entries
    }

    /** Download full attachment archives or individual files within those */
    open fun openAttachment(
            nodeName: Optional<String>,
            hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) {

        val subPath = if (req.pathInfo == null) ""
        else req.pathInfo.substringAfter("attachments/$hash/", missingDelimiterValue = "")
        val attachment = getNodeService(nodeName).openAttachment(hash)

        logger.debug("openAttachment, subPath: $subPath")
        resp.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
        resp.outputStream.use { _ ->
            if (subPath.isEmpty()) {
                resp.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$hash.zip\"")
                attachment.use { it.copyTo(resp.outputStream) }
            } else {
                val filename = subPath.split('/').last()
                logger.debug("openAttachment, filename: $filename")
                resp.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
                JarInputStream(attachment).use { it.extractFile(subPath, resp.outputStream) }
            }
        }
    }

    /**
     * Persist the given file(s) as a vault attachment.
     * A single JAR or ZIP file will be persisted as-is, otherwise a new archive will be created.
     */
    open fun saveAttachment(nodeName: Optional<String>,
                            files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> {
        val fileEntry = getNodeService(nodeName)
                .saveAttachment(toAttachment(files))
        val location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(fileEntry.hash)
        return ResponseEntity.created(location.toUri()).body(fileEntry)
    }

    /** Get the [CordaNodeService] matching the given name in application.properties */
    protected fun getNodeService(nodeName: Optional<String>) =
            this.networkService.getNodeService(nodeName)
}
