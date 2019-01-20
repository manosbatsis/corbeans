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
import io.swagger.annotations.ApiOperation
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
import org.springframework.web.bind.annotation.*
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
 *  Rest controller with basic endpoints for multiple corda nodes. Supports multiple nodes by
 *  using the <code>nodeName</code>  *  path variable to construct a Service Bean name,
 *  then uses it as a key to lookup and obtain  *  a CordaNodeService for the specific node
 *  from the autowired services map.
 */
// TODO: allow for autoconfigure only @ConditionalOnClass(value = Tomcat.class)
@RestController
@RequestMapping(path = ["api/node", "api/nodes/{nodeName}"])
class CordaNodesController {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesController::class.java)

    }

    @Autowired
    protected lateinit var networkService: CordaNetworkService

    @GetMapping("me")
    @ApiOperation(value = "Get the node's X500 principal name.")
    fun me(@PathVariable nodeName: Optional<String>) =
            mapOf("me" to networkService.getNodeService(nodeName).myIdentity.name.x500Principal.name.toString())

    @GetMapping("whoami")
    @ApiOperation(value = "Get the node's identity name.")
    fun whoami(@PathVariable nodeName: Optional<String>) =
            mapOf("me" to networkService.getNodeService(nodeName).myIdentity.name)


    @GetMapping("peers")
    @ApiOperation(value = "Get a list of the node's network peers.")
    fun peers(@PathVariable nodeName: Optional<String>) = this.networkService.getNodeService(nodeName).peers()

    @GetMapping("peernames")
    @ApiOperation(value = "Get a list of the node's network peer names.")
    fun peerNames(@PathVariable nodeName: Optional<String>) = this.networkService.getNodeService(nodeName).peerNames()

    @GetMapping("serverTime")
    @ApiOperation(value = "Get tbe node time in UTC.")
    fun serverTime(@PathVariable nodeName: Optional<String>): LocalDateTime {
        return this.networkService.getNodeService(nodeName).serverTime()
    }

    @GetMapping("addresses")
    @ApiOperation(value = "Get tbe node addresses.")
    fun addresses(@PathVariable nodeName: Optional<String>): List<NetworkHostAndPort> {
        return this.networkService.getNodeService(nodeName).addresses()
    }

    @GetMapping("identities")
    @ApiOperation(value = "Get tbe node identities.")
    fun identities(@PathVariable nodeName: Optional<String>): List<Party> {
        return this.networkService.getNodeService(nodeName).identities()
    }

    @GetMapping("platformVersion")
    @ApiOperation(value = "Get tbe node's platform version.")
    fun platformVersion(@PathVariable nodeName: Optional<String>): Int {
        return this.networkService.getNodeService(nodeName).platformVersion()
    }

    @GetMapping("flows")
    @ApiOperation(value = "Get tbe node flows.")
    fun flows(@PathVariable nodeName: Optional<String>): List<String> {
        return this.networkService.getNodeService(nodeName).flows()
    }

    @GetMapping("notaries")
    @ApiOperation(value = "Get tbe node notaries.")
    fun notaries(@PathVariable nodeName: Optional<String>): List<Party> {
        return this.networkService.getNodeService(nodeName).notaries()
    }

    @GetMapping("states")
    @ApiOperation(value = "Get tbe node states.")
    fun states(@PathVariable nodeName: Optional<String>): List<StateAndRef<ContractState>> {
        return this.networkService.getNodeService(nodeName).states()
    }

    @GetMapping("attachments/{hash}/paths")
    @ApiOperation(value = "List the contents of the attachment archive matching the given hash.")
    fun listAttachmentFiles(@PathVariable nodeName: Optional<String>,
                       @PathVariable hash: SecureHash): List<String>  {
        val entries = mutableListOf<String>()
        this.networkService.getNodeService(nodeName).openAttachment(hash).use { attachmentArchive ->
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

    @GetMapping("attachments/{hash}/**")
    @ApiOperation(
            value = "Download full attachment archives or individual files within those.",
            notes = "e.g. \"GET /attachments/123abcdef12121\" will return the archive identified by the given hash, while " +
                    "\"GET /attachments/123abcdef12121/foo.txt\" will return a specific file from within the attachment archive.")
    fun openAttachment(
            @PathVariable nodeName: Optional<String>,
            @PathVariable hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) {

        val subPath = if (req.pathInfo == null) ""
        else req.pathInfo.substringAfter("attachments/$hash/", missingDelimiterValue = "")
        val attachment = this.networkService.getNodeService(nodeName).openAttachment(hash)

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

    @PostMapping(value = ["attachments"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiOperation(value = "Persist the given file(s) as a vault attachment")
    fun saveAttachment(@PathVariable nodeName: Optional<String>,
                       @RequestParam(name = "file", required = true) files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> {
        val fileEntry = this.networkService.getNodeService(nodeName)
                .saveAttachment(toAttachment(files))
        val location = ServletUriComponentsBuilder
                .fromCurrentRequest().path("/{id}")
                .buildAndExpand(fileEntry.hash)
        return ResponseEntity.created(location.toUri()).body(fileEntry)
    }
}