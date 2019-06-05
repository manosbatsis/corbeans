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

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.manosbatsis.corbeans.corda.webserver.components.SampleCustomCordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.model.PartyNameModel
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentReceipt
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNetworkService
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension
import com.github.manosbatsis.corbeans.test.integration.WithImplicitNetworkIT
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
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * Same as [SingleNetworkIntegrationTest] only using [CorbeansSpringExtension]
 * instead of extending [WithImplicitNetworkIT]
 */
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

    @Test
    fun `Can use both default node and multiple node controller endpoints`() {
        val defaultNodeMe = this.restTemplate.getForObject("/api/node/whoami", PartyNameModel::class.java)
        assertNotNull(defaultNodeMe.organisation)
        val partyANodeMe = this.restTemplate.getForObject("/api/nodes/partyA/whoami", PartyNameModel::class.java)
        assertNotNull(partyANodeMe.organisation)
    }


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
    }

    @Test
    fun `Can retrieve peer identities`() {
        assertNotNull(service.identities())
    }

    @Test
    fun `Can retrieve notaries`() {
        val notaries: List<Party> = service.notaries()
        assertNotNull(notaries)
    }

    @Test
    fun `Can retrieve flows`() {
        val flows: List<String> = service.flows()
        assertNotNull(flows)
    }

    @Test
    fun `Can retrieve addresses`() {
        val addresses: List<NetworkHostAndPort> = service.addresses()
        assertNotNull(addresses)
    }

    @Test
    fun `Can handle object conversions`() {
        // convert to<>from SecureHash
        val hash = "6D1687C143DF792A011A1E80670A4E4E0C25D0D87A39514409B1ABFC2043581F"
        val hashEcho = this.restTemplate.getForEntity("/api/echo/echoSecureHash/${hash.toString()}", Any::class.java)
        logger.info("hashEcho body:  ${hashEcho.body}")
        assertEquals(hash, hashEcho.body)
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
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        // Upload a couple of files
        var attachmentReceipt: AttachmentReceipt = uploadAttachmentFiles(
                createMockMultipartFile("test.txt", "text/plain"),
                createMockMultipartFile("test.png", "image/png"))
        // Make sure the attachment has a hash, is not marked as original and contains all uploaded files
        assertNotNull(attachmentReceipt.hash)
        assertFalse(attachmentReceipt.savedOriginal)
        assertTrue(attachmentReceipt.files.containsAll(listOf("test.txt", "test.png")))
        // Test archive download
        mockMvc.perform(
                get("/api/nodes/partyA/attachments/${attachmentReceipt.hash}"))
                .andExpect(status().isOk)
                .andReturn()
        // Test archive file entry download
        mockMvc.perform(
                get("/api/nodes/partyA/attachments/${attachmentReceipt.hash}/test.txt"))
                .andExpect(status().isOk)
                .andReturn()

        // Test archive file browsing
        val paths = this.restTemplate.getForObject(
                "/api/nodes/partyA/attachments/${attachmentReceipt.hash}/paths",
                List::class.java)
        logger.info("attachment paths: $paths")
        assertTrue(paths.containsAll(listOf("test.txt", "test.png")))
    }

    @Test
    @Throws(Exception::class)
    fun `Can save and retrieve single zip and jar files as attachments`() {
        testArchiveUploadAndDownload("test.zip", "application/zip")
        testArchiveUploadAndDownload("test.jar", "application/java-archive")
    }

    private fun uploadAttachmentFiles(vararg  file: MockMultipartFile): AttachmentReceipt {
        var attachmentReceipt: AttachmentReceipt? = null
        val multipart = multipart("/api/nodes/partyA/attachments")
        file.forEach { multipart.file(it) }
        this.mockMvc
                .perform(multipart)
                .andExpect(status().isCreated)
                .andDo { mvcResult ->
                    val json = mvcResult.response.contentAsString
                    attachmentReceipt = objectMapper.readValue(json, AttachmentReceipt::class.java)
                }
                .andReturn()
        return attachmentReceipt!!
    }

    private fun testArchiveUploadAndDownload(fileName: String, mimeType: String) {
        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        // Add the archive to the upload
        val fileToUpload = createMockMultipartFile(fileName, mimeType)
        // Test upload
        var attachmentReceipt: AttachmentReceipt = uploadAttachmentFiles(fileToUpload)
        // Make sure the attachment has a hash, is marked as original and contains the uploaded archive
        assertNotNull(attachmentReceipt.hash)
        assertTrue(attachmentReceipt.savedOriginal)
        assertTrue(attachmentReceipt.files.contains(fileName))
        // Test archive download
        mockMvc.perform(
                get("/api/nodes/partyA/attachments/${attachmentReceipt.hash}"))
                .andExpect(status().isOk)
                .andReturn()
    }

    fun createMockMultipartFile(fileName: String, mimeType: String) =
            MockMultipartFile("file", fileName, mimeType,
                    this::class.java.getResourceAsStream("/uploadfiles/$fileName"))
}
