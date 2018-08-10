package com.github.manosbatsis.corda.webserver.spring

import com.github.manosbatsis.corda.spring.beans.CordaNodeService
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MultiNodeIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(MultiNodeIntegrationTest::class.java)
    }

    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService

    @Test
    fun `Can inject services`() {
        assertNotNull(this.services)
        logger.info("Auto-configured RESTful services for Corda nodes:: {}", this.services.keys)
        assertTrue(this.services.keys.isNotEmpty())
    }

    @Test
    fun `Can retreive node identity`() {
        val party = service.getMyIdentity()
        logger.info("My identity: {}", party)
        assertNotNull(service.getMyIdentity())
    }

    @Test
    fun `Can retreive notaries`() {
        val notaries: List<Party> = service.notaries()
        logger.info("Notaries: {}", notaries)
        assertNotNull(notaries)
    }

    @Test
    fun `Can retreive flows`() {
        val flows: List<String> = service.flows()
        logger.info("Flows: {}", flows)
        assertNotNull(flows)
    }

    @Test
    fun `Can retreive addresses`() {
        val addresses: List<NetworkHostAndPort> = service.addresses()
        logger.info("Addresses: {}", addresses)
        assertNotNull(addresses)
    }

}