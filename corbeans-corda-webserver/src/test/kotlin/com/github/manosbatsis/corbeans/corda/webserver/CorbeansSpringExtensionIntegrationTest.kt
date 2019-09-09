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
package com.github.manosbatsis.corbeans.corda.webserver

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.manosbatsis.corbeans.corda.webserver.components.SampleCustomCordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension
import com.github.manosbatsis.corbeans.test.integration.WithImplicitNetworkIT
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.core.io.ClassPathResource
import org.springframework.http.*
import org.springframework.test.web.servlet.MockMvc
import org.springframework.util.LinkedMultiValueMap
import java.util.*
import kotlin.test.assertTrue


/**
 * Same as [SingleNetworkIntegrationTest] only using [CorbeansSpringExtension]
 * instead of extending [WithImplicitNetworkIT]
 */
@Suppress("SpringJavaInjectionPointsAutowiringInspection")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
@AutoConfigureMockMvc
class CorbeansSpringExtensionIntegrationTest {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansSpringExtensionIntegrationTest::class.java)

    }

    // autowire a JSON object mapper
    @Autowired
    lateinit var objectMapper: ObjectMapper

    // autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    // autowire all created node services directly, mapped by name
    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    // autowire a node service for a specific node
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService

    // autowire a unique custom service
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var customCervice: SampleCustomCordaNodeServiceImpl

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    /* TODO: test single/multiple nodes using profiles
    @Test
    fun `Can use both default node and multiple node controller endpoints`() {
        val defaultNodeMe = this.restTemplate.getForObject("/api/node/whoami", PartyNameModel::class.java)
        assertNotNull(defaultNodeMe.organisation)
        val partyANodeMe = this.restTemplate.getForObject("/api/node/whoami", PartyNameModel::class.java)
        assertNotNull(partyANodeMe.organisation)
    }
    */


    @Test
    fun `Can inject services`() {
        logger.info("services: {}", services)
        assertNotNull(this.networkService)
        assertNotNull(this.services)
        assertNotNull(this.service)
        assertTrue(this.services.keys.isNotEmpty())
    }

    @Test
    fun `Can inject custom service`() {
        logger.info("customCervice: {}", customCervice)
        assertNotNull(this.customCervice)
        assertTrue(this.customCervice.dummy())
    }

    @Test
    fun `Can retrieve node identity`() {
        assertNotNull(service.myIdentity)
        val entity = this.restTemplate.getForEntity("/api/node/whoami", Any::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can retrieve identities`() {
        assertNotNull(service.identities())
        val entity = this.restTemplate.getForEntity("/api/node/identities", JsonNode::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve nodes`() {
        assertNotNull(service.identities())
        val entity = this.restTemplate.getForEntity("/api/node/nodes", JsonNode::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve peers`() {
        assertNotNull(service.identities())
        val entity = this.restTemplate.getForEntity("/api/node/peers", JsonNode::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve notaries`() {
        val notaries: List<Party> = service.notaries()
        assertNotNull(notaries)
        val entity = this.restTemplate.getForEntity("/api/node/notaries", JsonNode::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
        assertTrue {entity.body!!.toList().isNotEmpty() }
    }

    @Test
    fun `Can retrieve flows`() {
        val flows: List<String> = service.flows()
        assertNotNull(flows)
        val entity = this.restTemplate.getForEntity("/api/node/flows", Any::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can retrieve addresses`() {
        val addresses: List<NetworkHostAndPort> = service.addresses()
        assertNotNull(addresses)
        val entity = this.restTemplate.getForEntity("/api/node/addresses", Any::class.java)
        assertEquals(HttpStatus.OK, entity.statusCode)
        assertEquals(MediaType.APPLICATION_JSON.type, entity.headers.contentType?.type)
        assertEquals(MediaType.APPLICATION_JSON.subtype, entity.headers.contentType?.subtype)
    }

    @Test
    fun `Can handle object conversions`() {
        // convert to<>from SecureHash
        val hash = "6D1687C143DF792A011A1E80670A4E4E0C25D0D87A39514409B1ABFC2043581F"
        val hashEcho = this.restTemplate.getForEntity("/api/echo/echoSecureHash/${hash}", Any::class.java)
        logger.info("hashEcho body:  ${hashEcho.body}")
        assertEquals(hash, hashEcho.body)
        // convert to<>from UniqueIdentifier, including external ID with underscore
        val linearId = UniqueIdentifier("foo_bar-baz", UUID.randomUUID())
        val linearIdEcho = this.restTemplate.getForEntity("/api/echo/echoUniqueIdentifier/${linearId}", UniqueIdentifier::class.java)
        logger.info("linearIdEcho body:  ${linearIdEcho.body}")
        assertEquals(linearId, linearIdEcho.body)
        // convert to<>from CordaX500Name
        val cordaX500Name = CordaX500Name.parse("O=Bank A, L=New York, C=US, OU=Org Unit, CN=Service Name")
        val cordaX500NameEcho = this.restTemplate
                .getForEntity("/api/echo/echoCordaX500Name/$cordaX500Name", Any::class.java)
        logger.info("cordaX500NameEcho body: ${cordaX500NameEcho.body}")
        assertEquals(cordaX500Name, CordaX500Name.parse(cordaX500NameEcho.body.toString()))
    }


    @Test
    @Throws(Exception::class)
    fun `Can save and retrieve regular files as attachments`() {
        // Upload a couple of files
        var attachmentReceipt: JsonNode = uploadAttachmentFiles(
                Pair("test.txt", "text/plain"),
                Pair("test.png", "image/png"))
        // Make sure the attachment has a hash, is not marked as original and contains all uploaded files
        val hash = attachmentReceipt.get("hash").asText()
        assertNotNull(hash)
        // TODO assertTrue(attachmentReceipt.files.containsAll(listOf("test.txt", "test.png")))
        // Test archive download
        var attachment = this.restTemplate.getForEntity("/api/node/attachments/${hash}", ByteArray::class.java)
        assertEquals(HttpStatus.OK, attachment.statusCode)
        // Test archive file entry download
        attachment = this.restTemplate.getForEntity("/api/node/attachments/${hash}/test.txt", ByteArray::class.java)
        assertEquals(HttpStatus.OK, attachment.statusCode)

        // Test archive file browsing
        val paths = this.restTemplate.getForObject(
                "/api/node/attachments/${hash}/paths",
                List::class.java)
        logger.info("attachment paths: $paths")
        assertTrue(paths.containsAll(listOf("test.txt", "test.png")))
    }

    @Test
    @Throws(Exception::class)
    fun `Can save and retrieve single zip and jar files as attachments`() {
        testArchiveUploadAndDownload("test.zip", "application/zip")
        testArchiveUploadAndDownload("test.jar", "application/java-archive")
        // Ensure a proper 404
        val attachment = this.restTemplate.getForEntity("/api/node/attachments/${SecureHash.randomSHA256()}", ByteArray::class.java)
        assertEquals(HttpStatus.NOT_FOUND, attachment.statusCode)

    }

    private fun uploadAttachmentFiles(vararg files: Pair<String, String>): JsonNode {
        val parameters = LinkedMultiValueMap<String, Any>()
        files.forEach {
            parameters.add("file", ClassPathResource("/uploadfiles/${it.first}"))
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA

        val entity = HttpEntity(parameters, headers)

        val response = this.restTemplate.exchange("/api/node/attachments",
                HttpMethod.POST, entity, JsonNode::class.java, "")

        var attachmentReceipt: JsonNode? = response.body
        logger.info("uploadAttachmentFiles, attachmentReceipt: ${attachmentReceipt.toString()}")
        return attachmentReceipt!!
    }

    private fun testArchiveUploadAndDownload(fileName: String, mimeType: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        // Add the archive to the upload
        // Test upload
        var attachmentReceipt: JsonNode = uploadAttachmentFiles(Pair(fileName, mimeType))
        // Make sure the attachment has a hash, is marked as original and contains the uploaded archive
        val hash = attachmentReceipt.get("hash").asText()
        assertNotNull(hash)
        assertTrue(attachmentReceipt.get("savedOriginal").asBoolean())
        //TODO assertNotNull(attachmentReceipt.withArray("files").toList().find{it == fileName}.singleOrNull())
        // Test archive download
        val attachment = this.restTemplate.getForEntity("/api/node/attachments/${hash}", ByteArray::class.java)
        assertEquals(HttpStatus.OK, attachment.statusCode)
    }


}
