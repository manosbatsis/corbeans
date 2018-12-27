/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
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
package com.github.manosbatsis.corbeans.spring.boot.corda

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
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.io.FileNotFoundException
import java.time.LocalDateTime
import java.util.*
import java.util.jar.JarInputStream
import javax.annotation.PostConstruct
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
@RequestMapping(path = arrayOf("node", "nodes/{nodeName}"))
open class CordaNodesController {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesController::class.java)
        val NODE_NAME_DEFAULT = "default"
    }

    @Autowired
    protected lateinit var services: Map<String, CordaNodeService>// = HashMap()

    @PostConstruct
    fun postConstruct() {
        logger.debug("Auto-configured RESTful services for Corda nodes:: {}", services.keys)
    }

    fun getService(optionalNodeName: Optional<String>): CordaNodeService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else NODE_NAME_DEFAULT
        return this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")
    }

    /** Returns the node's name. */
    @GetMapping("me")
    fun me(@PathVariable nodeName: Optional<String>) = mapOf("me" to getService(nodeName).myIdentity.name.x500Principal.name.toString())

    /** Returns the node info. */
    @GetMapping("whoami")
    fun whoami(@PathVariable nodeName: Optional<String>) = mapOf("me" to getService(nodeName).myIdentity.name)
    //fun me() = mapOf("me" to _myIdentity.name.x500Principal.name.toString())

    /** Returns a list of the node's network peers. */
    @GetMapping("peers")
    fun peers(@PathVariable nodeName: Optional<String>) = this.getService(nodeName).peers()

    /** Returns a list of the node's network peer names. */
    @GetMapping("peernames")
    fun peerNames(@PathVariable nodeName: Optional<String>) = this.getService(nodeName).peerNames()

    /** Return tbe node time in UTC */
    @GetMapping("serverTime")
    fun serverTime(@PathVariable nodeName: Optional<String>): LocalDateTime {
        return this.getService(nodeName).serverTime()
    }

    @GetMapping("addresses")
    fun addresses(@PathVariable nodeName: Optional<String>): List<NetworkHostAndPort> {
        return this.getService(nodeName).addresses()
    }

    @GetMapping("identities")
    fun identities(@PathVariable nodeName: Optional<String>): List<Party> {
        return this.getService(nodeName).identities()
    }

    @GetMapping("platformVersion")
    fun platformVersion(@PathVariable nodeName: Optional<String>): Int {
        return this.getService(nodeName).platformVersion()
    }

    @GetMapping("flows")
    fun flows(@PathVariable nodeName: Optional<String>): List<String> {
        return this.getService(nodeName).flows()
    }

    @GetMapping("notaries")
    fun notaries(@PathVariable nodeName: Optional<String>): List<Party> {
        return this.getService(nodeName).notaries()
    }

    @GetMapping("states")
    fun states(@PathVariable nodeName: Optional<String>): List<StateAndRef<ContractState>> {
        return this.getService(nodeName).states()
    }

    /**
     * Allows the node administrator to either download full attachment zips, or individual files within those zips.
     *
     * GET /attachments/123abcdef12121            -> download the zip identified by this hash
     * GET /attachments/123abcdef12121/foo.txt    -> download that file specifically
     *
     * Files are always forced to be downloads, they may not be embedded into web pages for security reasons.
     *
     * TODO: Provide an endpoint that exposes attachment file listings, to make attachments browsable.
     */
    @GetMapping("attachment/{id}/**")
    fun openArrachment(@PathVariable nodeName: Optional<String>, @PathVariable id: String, req: HttpServletRequest, resp: HttpServletResponse) {

        val reqPath = req.pathInfo?.substring(1)
        if (reqPath == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST)
            return
        }

        try {
            val hash = SecureHash.parse(reqPath.substringBefore('/'))
            //val service = this.getService(nodeName)
            val attachment = this.getService(nodeName).openArrachment(hash)

            // Don't allow case sensitive matches inside the jar, it'd just be confusing.
            val subPath = reqPath.substringAfter('/', missingDelimiterValue = "").toLowerCase()

            resp.contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE
            if (subPath.isEmpty()) {
                resp.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$hash.zip\"")
               attachment.use { it.copyTo(resp.outputStream) }
            } else {
                val filename = subPath.split('/').last()
                resp.addHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"$filename\"")
                JarInputStream(attachment).use { it.extractFile(subPath, resp.outputStream) }
            }

            // Closing the output stream commits our response. We cannot change the status code after this.
            resp.outputStream.close()
        } catch (e: FileNotFoundException) {
            logger.warn("404 Not Found whilst trying to handle attachment download request for ${nodeName}, path$reqPath")
            resp.sendError(HttpServletResponse.SC_NOT_FOUND)
            return
        }
    }



    /**
     * Converts a MultipartFile to a File DTO
    protected fun toFile(toFile: MultipartFile) = File(
            name = toFile.name,
            originalFilename = toFile.originalFilename,
            inputStream = toFile.inputStream,
            size = toFile.size,
            contentType = toFile.contentType)
     */


}