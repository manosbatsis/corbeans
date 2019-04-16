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
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import org.slf4j.LoggerFactory
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.time.LocalDateTime
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


/**
 *  Exposes [CorbeansBaseController] methods as endpoints.
 *  Supports multiple Corda nodes via an optional <code>nodeName</code> path variable.
 *  When provided, the `nodeName` is used to obtain  an autoconfigured  `CordaNodeService`
 *  for the node configuration matching `nodeName` in application properties.
 */
@RestController
@RequestMapping(path = ["api/node", "api/nodes/{nodeName}"])
@Api(tags = arrayOf("Corda Node Services"), description = "Generic Corda node operations")
class CordaNodesController : CorbeansBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesController::class.java)
    }

    @GetMapping("me")
    @ApiOperation(value = "Get the node's X500 principal name.")
    override fun me(@PathVariable nodeName: Optional<String>) = super.me(nodeName)

    @GetMapping("whoami")
    @ApiOperation(value = "Get the node's identity name.")
    override fun whoami(@PathVariable nodeName: Optional<String>) = super.whoami(nodeName)

    @GetMapping("nodes")
    @ApiOperation(value = "Get a list of nodes in the network.")
    override fun nodes(@PathVariable nodeName: Optional<String>) = super.nodes(nodeName)


    @GetMapping("peers")
    @ApiOperation(value = "Get a list of the node's network peers.")
    override fun peers(@PathVariable nodeName: Optional<String>) = super.peers(nodeName)

    @GetMapping("peernames")
    @ApiOperation(value = "Get a list of the node's network peer names.")
    override fun peerNames(@PathVariable nodeName: Optional<String>) = super.peerNames(nodeName)

    @GetMapping("serverTime")
    @ApiOperation(value = "Get tbe node time in UTC.")
    override fun serverTime(@PathVariable nodeName: Optional<String>): LocalDateTime = super.serverTime(nodeName)

    @GetMapping("addresses")
    @ApiOperation(value = "Get tbe node addresses.")
    override fun addresses(@PathVariable nodeName: Optional<String>): List<NetworkHostAndPort> = super.addresses(nodeName)

    @GetMapping("identities")
    @ApiOperation(value = "Get tbe node identities.")
    override fun identities(@PathVariable nodeName: Optional<String>): List<Party> = super.identities(nodeName)

    @GetMapping("platformVersion")
    @ApiOperation(value = "Get tbe node's platform version.")
    override fun platformVersion(@PathVariable nodeName: Optional<String>): Int = super.platformVersion(nodeName)

    @GetMapping("flows")
    @ApiOperation(value = "Get tbe node flows.")
    override fun flows(@PathVariable nodeName: Optional<String>): List<String> = super.flows(nodeName)

    @GetMapping("notaries")
    @ApiOperation(value = "Get tbe node notaries.")
    override fun notaries(@PathVariable nodeName: Optional<String>): List<Party> = super.notaries(nodeName)

    @GetMapping("states")
    @ApiOperation(value = "Get tbe node states.")
    override fun states(@PathVariable nodeName: Optional<String>): List<StateAndRef<ContractState>> = super.states(nodeName)

    @GetMapping("attachments/{hash}/paths")
    @ApiOperation(value = "List the contents of the attachment archive matching the given hash.")
    override fun listAttachmentFiles(@PathVariable nodeName: Optional<String>,
                                     @PathVariable hash: SecureHash): List<String> = super.listAttachmentFiles(nodeName, hash)

    @GetMapping("attachments/{hash}/**")
    @ApiOperation(
            value = "Download full attachment archives or individual files within those.",
            notes = "e.g. \"GET /attachments/123abcdef12121\" will return the archive identified by the given hash, while " +
                    "\"GET /attachments/123abcdef12121/foo.txt\" will return a specific file from within the attachment archive.")
    override fun openAttachment(
            @PathVariable nodeName: Optional<String>,
            @PathVariable hash: SecureHash, req: HttpServletRequest, resp: HttpServletResponse) =
            super.openAttachment(nodeName, hash, req, resp)

    @PostMapping(value = ["attachments"], consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @ApiOperation(value = "Persist the given file(s) as a vault attachment. " +
            "A single JAR or ZIP file will be persisted as-is, otherwise a new archive will be created.")
    override fun saveAttachment(@PathVariable nodeName: Optional<String>,
                                @RequestParam(name = "file", required = true) files: Array<MultipartFile>
    ): ResponseEntity<AttachmentReceipt> = super.saveAttachment(nodeName, files)
}
