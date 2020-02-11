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

import com.github.manosbatsis.corbeans.spring.boot.corda.model.PartyNameModel
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentReceipt
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import net.corda.core.crypto.SecureHash
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.Optional
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 *  Exposes [CorbeansBaseController] methods as endpoints.
 *  Supports multiple Corda nodes via a <code>nodeName</code> path variable.
 *  The `nodeName` is used to obtain  an autoconfigured  `CordaNodeService`
 *  for the node configuration matching `nodeName` in application properties.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyNodeController: CordaPathFragmentNodeController()
 *  ```
 *
 *  @see CordaNodeController
 */
@RequestMapping(path = ["api/nodes/{nodeName}"])
@Tag(name = "Corda Node Services", description = "Operations for multiple Corda nodes")
open class CordaPathFragmentNodeController : CorbeansBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaPathFragmentNodeController::class.java)
    }

    @GetMapping("nodeNamesByOrgName")
    @Operation(summary = "Get the configured node names by org name.")
    fun nodeNamesByOrgName() = networkService.nodeNamesByOrgName

    @GetMapping("whoami")
    @Operation(summary = "Get the node's identity.")
    override fun whoami(@PathVariable nodeName: Optional<String>): PartyNameModel = super.whoami(nodeName)

    @GetMapping("nodes")
    @Operation(summary = "Get a list of nodes in the network, including self and notaries.")
    override fun nodes(@PathVariable nodeName: Optional<String>): List<PartyNameModel> = super.nodes(nodeName)

    @GetMapping("notaries")
    @Operation(summary = "Get tbe node notaries.")
    override fun notaries(@PathVariable nodeName: Optional<String>): List<PartyNameModel> = super.notaries(nodeName)

    @GetMapping("peers")
    @Operation(summary = "Get a list of the node's network peers, excluding self and notaries.")
    override fun peers(@PathVariable nodeName: Optional<String>): List<PartyNameModel> = super.peers(nodeName)

    @GetMapping("serverTime")
    @Operation(summary = "Get tbe node time in UTC.")
    override fun serverTime(@PathVariable nodeName: Optional<String>): LocalDateTime = super.serverTime(nodeName)

    @GetMapping("addresses")
    @Operation(summary = "Get tbe node addresses.")
    override fun addresses(@PathVariable nodeName: Optional<String>): List<NetworkHostAndPort> = super.addresses(nodeName)

    @GetMapping("identities")
    @Operation(summary = "Get tbe node identities.")
    override fun identities(@PathVariable nodeName: Optional<String>): List<PartyNameModel> = super.identities(nodeName)

    @GetMapping("platformVersion")
    @Operation(summary = "Get tbe node's platform version.")
    override fun platformVersion(@PathVariable nodeName: Optional<String>): Int = super.platformVersion(nodeName)

    @GetMapping("flows")
    @Operation(summary = "Get tbe node flows.")
    override fun flows(@PathVariable nodeName: Optional<String>): List<String> = super.flows(nodeName)

    @GetMapping("attachments/{hash}/paths")
    @Operation(summary = "List the contents of the attachment archive matching the given hash.")
    override fun listAttachmentFiles(@PathVariable nodeName: Optional<String>,
                                     @PathVariable hash: SecureHash): List<String> = super.listAttachmentFiles(nodeName, hash)

    @GetMapping("attachments/{hash}/**")
    @Operation(
            summary = "Download full attachment archives or individual files within those.",
            description = "e.g. \"GET /attachments/123abcdef12121\" will return the archive identified by the given hash, while " +
                    "\"GET /attachments/123abcdef12121/foo.txt\" will return a specific file from within the attachment archive.")
    override fun openAttachment(
            @PathVariable nodeName: Optional<String>,
            @PathVariable hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) =
            super.openAttachment(nodeName, hash, req, resp)

    @PostMapping(value = ["attachments"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Persist the given file(s) as a vault attachment. " +
            "A single JAR or ZIP file will be persisted as-is, otherwise a new archive will be created.")
    override fun saveAttachment(@PathVariable nodeName: Optional<String>,
                                @RequestParam(name = "file", required = true) files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> = super.saveAttachment(nodeName, files)
}
