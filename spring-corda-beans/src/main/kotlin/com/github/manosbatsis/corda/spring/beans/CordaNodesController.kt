package com.github.manosbatsis.corda.spring.beans

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
import java.util.jar.JarInputStream
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 *  Rest controller for multiple corda nodes
 */
// TODO: allow for autoconfigure only @ConditionalOnClass(value = Tomcat.class)
@RestController
@RequestMapping("nodes")
open class CordaNodesController {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodesController::class.java)
    }

    @Autowired
    protected lateinit var services: Map<String, CordaNodeService>// = HashMap()

    @PostConstruct
    fun postConstruct() {
        logger.info("Auto-configured RESTful services for Corda nodes:: {}", services.keys)
    }

    fun getService(nodeName: String): CordaNodeService =
            this.services.get("${nodeName}NodeService") ?: throw IllegalArgumentException("Node not found: $nodeName")

    /** Returns the node's name. */
    @GetMapping("{nodeName}/me")
    fun me(@PathVariable nodeName: String) = mapOf("me" to getService(nodeName).getMyIdentity().name.x500Principal.name.toString())

    /** Returns the node info. */
    @GetMapping("{nodeName}/whoami")
    fun whoami(@PathVariable nodeName: String) = mapOf("me" to getService(nodeName).getMyIdentity().name)
    //fun me() = mapOf("me" to _myIdentity.name.x500Principal.name.toString())

    /** Returns a list of the node's network peers. */
    @GetMapping("{nodeName}/peers")
    fun peers(@PathVariable nodeName: String) = this.getService(nodeName).peers()

    /** Returns a list of the node's network peer names. */
    @GetMapping("{nodeName}/peernames")
    fun peerNames(@PathVariable nodeName: String) = this.getService(nodeName).peerNames()

    /** Return tbe node time in UTC */
    @GetMapping("{nodeName}/serverTime")
    fun serverTime(@PathVariable nodeName: String): LocalDateTime {
        return this.getService(nodeName).serverTime()
    }

    @GetMapping("{nodeName}/addresses")
    fun addresses(@PathVariable nodeName: String): List<NetworkHostAndPort> {
        return this.getService(nodeName).addresses()
    }

    @GetMapping("{nodeName}/identities")
    fun identities(@PathVariable nodeName: String): List<Party> {
        return this.getService(nodeName).identities()
    }

    @GetMapping("{nodeName}/platformVersion")
    fun platformVersion(@PathVariable nodeName: String): Int {
        return this.getService(nodeName).platformVersion()
    }

    @GetMapping("{nodeName}/flows")
    fun flows(@PathVariable nodeName: String): List<String> {
        return this.getService(nodeName).flows()
    }

    @GetMapping("{nodeName}/notaries")
    fun notaries(@PathVariable nodeName: String): List<Party> {
        return this.getService(nodeName).notaries()
    }

    @GetMapping("{nodeName}/states")
    fun states(@PathVariable nodeName: String): List<StateAndRef<ContractState>> {
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
     * TODO: See if there's a way to prevent access by JavaScript.
     * TODO: Provide an endpoint that exposes attachment file listings, to make attachments browsable.
     */
    @GetMapping("{nodeName}/attachment/{id}/**")
    fun openArrachment(@PathVariable nodeName: String, @PathVariable id: String, req: HttpServletRequest, resp: HttpServletResponse) {
        /*
        val attachment = this.service.findAttachment(SecureHash.parse(id))

        response.contentType = MimetypesFileTypeMap().getContentType(attachment.filename);
        response.setHeader("Content-disposition", "attachment; filename=${attachment.filename}");
        IOUtils.copy(attachment.inputStream, response.outputStream)
        IOUtils.closeQuietly(attachment.inputStream)
        */
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