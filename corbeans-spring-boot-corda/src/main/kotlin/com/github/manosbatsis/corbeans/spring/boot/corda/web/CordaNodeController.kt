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
 *  Exposes [CorbeansBaseController] methods as endpoints for a single node or,
 *  by overriding `getRequestNodeName()`, multiple nodes.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyNodeController: CordaNodeController()
 *  ```
 *
 *  @see CordaPathFragmentNodeController
 */
@RequestMapping(path = ["api/node"])
@Tag(name = "Corda Single Node Services", description = "Generic Corda (single) node operations")
open class CordaNodeController : CorbeansBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodeController::class.java)
    }

    /** Override to control how the the node name is resolved based on the request by e.g. parsing headers */
    open fun getRequestNodeName(): Optional<String> = Optional.empty()
    
    @GetMapping("nodeNamesByOrgName")
    @Operation(summary = "Get the configured node names by org name.")
    fun nodeNamesByOrgName() = networkService.nodeNamesByOrgName

    @GetMapping("whoami")
    @Operation(summary = "Get the node's identity.")
    fun whoami() = super.whoami(getRequestNodeName())

    @GetMapping("nodes")
    @Operation(summary = "Get a list of nodes in the network, including self and notaries.")
    fun nodes() = super.nodes(getRequestNodeName())

    @GetMapping("peers")
    @Operation(summary = "Get a list of the node's network peers, excluding self and notaries.")
    fun peers() = super.peers(getRequestNodeName())

    @GetMapping("serverTime")
    @Operation(summary = "Get tbe node time in UTC.")
    fun serverTime(): LocalDateTime = super.serverTime(getRequestNodeName())

    @GetMapping("addresses")
    @Operation(summary = "Get tbe node addresses.")
    fun addresses(): List<NetworkHostAndPort> = super.addresses(getRequestNodeName())

    @GetMapping("identities")
    @Operation(summary = "Get tbe node identities.")
    fun identities(): List<PartyNameModel> = super.identities(getRequestNodeName())

    @GetMapping("platformVersion")
    @Operation(summary = "Get tbe node's platform version.")
    fun platformVersion(): Int = super.platformVersion(getRequestNodeName())

    @GetMapping("flows")
    @Operation(summary = "Get tbe node flows.")
    fun flows(): List<String> = super.flows(getRequestNodeName())

    @GetMapping("notaries")
    @Operation(summary = "Get tbe node notaries.")
    fun notaries(): List<PartyNameModel> = super.notaries(getRequestNodeName())

    @GetMapping("attachments/{hash}/paths")
    @Operation(summary = "List the contents of the attachment archive matching the given hash.")
    fun listAttachmentFiles(@PathVariable hash: SecureHash): List<String> = super.listAttachmentFiles(getRequestNodeName(), hash)

    @GetMapping("attachments/{hash}/**")
    @Operation(
            summary = "Download full attachment archives or individual files within those.",
            description = "e.g. \"GET /attachments/123abcdef12121\" will return the archive identified by the given hash, while " +
                    "\"GET /attachments/123abcdef12121/foo.txt\" will return a specific file from within the attachment archive.")
    fun openAttachment(@PathVariable hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) =
            super.openAttachment(getRequestNodeName(), hash, req, resp)

    @PostMapping(value = ["attachments"])
    @Operation(summary = "Persist the given file(s) as a vault attachment.",
            description = "A single JAR or ZIP file will be persisted as-is, otherwise a new archive will be created.")
    fun saveAttachment(@RequestParam(name = "file", required = true) files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> = super.saveAttachment(getRequestNodeName(), files)
}
