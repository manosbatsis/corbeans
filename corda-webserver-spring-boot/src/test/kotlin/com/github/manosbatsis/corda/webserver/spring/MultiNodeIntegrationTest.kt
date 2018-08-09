package com.github.manosbatsis.corda.webserver.spring

import com.github.manosbatsis.corda.spring.beans.CordaNodeService
import net.corda.core.utilities.getOrThrow
import net.corda.testing.core.DUMMY_BANK_A_NAME
import net.corda.testing.driver.DriverParameters
import net.corda.testing.driver.NodeHandle
import net.corda.testing.driver.driver
import net.corda.testing.node.User
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.test.context.junit.jupiter.SpringExtension

@ExtendWith(SpringExtension::class)
//@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest(classes = arrayOf(Application::class), webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MultiNodeIntegrationTest(@Autowired val restTemplate: TestRestTemplate) {
    companion object {
        private val logger = LoggerFactory.getLogger(MultiNodeIntegrationTest::class.java)

        lateinit var nodes: NodeHandle//List<NodeHandle>

        @BeforeAll
        @JvmStatic
        fun setupNodes() {
            try {

                logger.info("startNodes")

                val inProcess = true
                val user = User("user1", "test", setOf("ALL"))
                driver(
                        /*DriverParameters(
                                //isDebug = true,
                                notarySpecs = emptyList(),
                                //useTestClock = true,
                                waitForAllNodesToFinish = true/*,
                                startNodesInProcess = inProcess*/)*/
                                DriverParameters(
                                        startNodesInProcess = false,
                                        notarySpecs = emptyList()// cordappsForAllNodes = emptySet()
                                        )
                                ) {

                    nodes = //istOf(
                            startNode(providedName = DUMMY_BANK_A_NAME,
                                    //startInSameProcess = inProcess,
                                    rpcUsers = listOf(user),
                                    customOverrides = mapOf(
                                            "rpcSettings.address" to "localhost:10008",
                                            "rpcSettings.adminAddress" to "localhost:10048")).getOrThrow()


                            /*,
                            startNode(providedName = DUMMY_BANK_B_NAME,
                                    startInSameProcess = inProcess,
                                    rpcUsers = listOf(user),
                                    customOverrides = mapOf(
                                            "rpcSettings.address" to "localhost:10011",
                                            "rpcSettings.adminAddress" to "localhost:10051"))*/
                    //).map { (it.getOrThrow()) }

                    //nodes.forEach {
                       // logger.info("startNodes node: $it")
                    //}

                }
            } catch (e: Exception) {

                logger.error("Failed starting test nodes ", e)
                e.printStackTrace()
                //teardownNodes()
                throw RuntimeException(e)
            }
        }

        @AfterAll
        @JvmStatic
        fun teardownNodes() {
            try {
                logger.info("teardownNodes...")
                //if (::nodes.isInitialized) {
                //nodes.forEach {
               //     logger.info("Stopping node: {}", it)
                    ///it.stop()
                nodes.stop()
               // }
                //}
            } catch (e: Exception) {

                logger.error("Failed teardownNodess {}", e)
                //e.printStackTrace()
                //throw RuntimeException(e)
            }
        }
    }

    @Autowired
    protected lateinit var services: Map<String, CordaNodeService>


    @Test
    fun `Can retreive node identity`() {
        logger.info("Auto-configured RESTful services for Corda nodes:: {}", this.services.keys)
        val service = if (this.services.keys.isNotEmpty()) this.services.get(services.keys.first()) else null
        assertNotNull(service)
    }

}