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
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.crypto.SecureHash
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 *  Exposes [CorbeansBaseController] methods as endpoints for a single node.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyCordaSingleNodeController: CordaSingleNodeController()
 *  ```
 *
 *  @see CordaNodesController
 */
@RequestMapping(path = ["api/node"])
@Api(tags = arrayOf("Corda Single Node Services"), description = "Generic Corda (single) node operations")
open class CordaSingleNodeController : CorbeansBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaSingleNodeController::class.java)
    }

    /** Override to control how the the node name is resolved based on the request by e.g. parsing headers */
    open fun getRequestNodeName(): Optional<String> = Optional.empty()
    
    @GetMapping("nodeNamesByOrgName")
    @ApiOperation(value = "Get the configured node names by org name.")
    fun nodeNamesByOrgName() = networkService.nodeNamesByOrgName

    @GetMapping("whoami")
    @ApiOperation(value = "Get the node's identity.")
    fun whoami() = super.whoami(getRequestNodeName())

    @GetMapping("nodes")
    @ApiOperation(value = "Get a list of nodes in the network, including self and notaries.")
    fun nodes() = super.nodes(getRequestNodeName())

    @GetMapping("peers")
    @ApiOperation(value = "Get a list of the node's network peers, excluding self and notaries.")
    fun peers() = super.peers(getRequestNodeName())

    @GetMapping("serverTime")
    @ApiOperation(value = "Get tbe node time in UTC.")
    fun serverTime(): LocalDateTime = super.serverTime(getRequestNodeName())

    @GetMapping("addresses")
    @ApiOperation(value = "Get tbe node addresses.")
    fun addresses(): List<NetworkHostAndPort> = super.addresses(getRequestNodeName())

    @GetMapping("identities")
    @ApiOperation(value = "Get tbe node identities.")
    fun identities(): List<PartyNameModel> = super.identities(getRequestNodeName())

    @GetMapping("platformVersion")
    @ApiOperation(value = "Get tbe node's platform version.")
    fun platformVersion(): Int = super.platformVersion(getRequestNodeName())

    @GetMapping("flows")
    @ApiOperation(value = "Get tbe node flows.")
    fun flows(): List<String> = super.flows(getRequestNodeName())

    @GetMapping("notaries")
    @ApiOperation(value = "Get tbe node notaries.")
    fun notaries(): List<PartyNameModel> = super.notaries(getRequestNodeName())

    @GetMapping("attachments/{hash}/paths")
    @ApiOperation(value = "List the contents of the attachment archive matching the given hash.")
    fun listAttachmentFiles(@PathVariable hash: SecureHash): List<String> = super.listAttachmentFiles(getRequestNodeName(), hash)

    @GetMapping("attachments/{hash}/**")
    @ApiOperation(
            value = "Download full attachment archives or individual files within those.",
            notes = "e.g. \"GET /attachments/123abcdef12121\" will return the archive identified by the given hash, while " +
                    "\"GET /attachments/123abcdef12121/foo.txt\" will return a specific file from within the attachment archive.")
    fun openAttachment(@PathVariable hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) =
            super.openAttachment(getRequestNodeName(), hash, req, resp)

    @PostMapping(value = ["attachments"])
    @ApiOperation(value = "Persist the given file(s) as a vault attachment. " +
            "A single JAR or ZIP file will be persisted as-is, otherwise a new archive will be created.")
    fun saveAttachment(@RequestParam(name = "file", required = true) files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> = super.saveAttachment(getRequestNodeName(), files)
}
