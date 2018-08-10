package com.github.manosbatsis.corda.webserver.spring

import com.github.manosbatsis.corda.spring.beans.CordaNodeService
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MultiNodeIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(MultiNodeIntegrationTest::class.java)
    }

    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    @Test
    fun `Can retreive node identity`() {
        logger.info("Auto-configured RESTful services for Corda nodes:: {}", this.services.keys)
        val service = if (this.services.keys.isNotEmpty()) this.services.get(services.keys.first()) else null
        assertNotNull(service)
    }

}