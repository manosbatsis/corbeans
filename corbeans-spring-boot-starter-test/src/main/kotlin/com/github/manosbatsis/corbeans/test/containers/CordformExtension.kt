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
package com.github.manosbatsis.corbeans.test.containers

import com.github.dockerjava.api.model.ExposedPort
import com.typesafe.config.ConfigFactory
import net.corda.nodeapi.internal.config.UnknownConfigKeysPolicy
import net.corda.nodeapi.internal.config.parseAs
import org.apache.commons.io.FileUtils
import org.junit.jupiter.api.extension.ExtensionContext
import org.slf4j.LoggerFactory
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.testcontainers.containers.BindMode.READ_WRITE
import org.testcontainers.containers.Network
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.utility.DockerImageName
import java.io.File
import java.nio.file.Paths
import java.time.Duration


/**
 *
 * Alternative JUnit extension VS subclassing [WithImplicitNetworkIT]. To be used instead of [SpringExtension]
 * Automatically creates and maintains a single Corda network throughout test execution,
 * using the corbeans' config from `application.properties`. You may override the latter with an
 * additional file in your test classpath, i.e. `src/test/resources/application.properties`.
 *
 * Example:
 *
 * ```
 * @SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
 * // Use CorbeansSpringExtension instead of SpringExtension
 * @ExtendWith(CorbeansSpringExtension::class)
 * class MyWithSingleNetworkIntegrationTest {
 *
 *      // autowire a service for a specific node
 *      @Autowired
 *      @Qualifier("partyANodeService")
 *      lateinit var service: CordaNodeService
 *
 *      // autowire a unique-typed custom node service
 *      @Autowired
 *      lateinit var customCervice: SampleCustomCordaNodeServiceImpl
 *
 *      @Test
 *      fun `Can inject services`() {
 *          assertNotNull(this.service)
 *          assertNotNull(this.customCervice)
 *      }
 *
 *      @Test
 *      fun `Can retrieve node identity`() {
 *          assertNotNull(service.myIdentity)
 *      }
 * }
 * ```
 */
open class CordformExtension : SpringExtension() {

    companion object {
        private val logger = LoggerFactory.getLogger(CordformExtension::class.java)

        @JvmStatic
        public var instances: MutableMap<String, KImageNameContainer> = mutableMapOf()
        @JvmStatic
        public var instancePorts: MutableMap<String, Int> = mutableMapOf()

    }

    /**
     * Delegate to [SpringExtension.beforeAll],
     * then start the Corda network
     */
    @Throws(Exception::class)
    override fun beforeAll(context: ExtensionContext) {
        startContainers(context)
        //updateProperties(context, container)
        super.beforeAll(context)
    }

    data class FsBindDirs(
            val nodesDir: File,
            val netParamsFile: File = File(nodesDir, "network-parameters"),
            val nodeInfosDir: File = File(nodesDir, "additional-node-infos").apply { mkdirs() }
    ){

        private val allSubDirs = nodesDir.listFiles { file ->
            file.isDirectory && File(file, "node.conf").exists()
        }.takeIf { it.isNotEmpty() } ?: error("Could not find any node directories")

        val notaryNodeDirs: List<File>
            get() = allSubDirs.filter { it.name.contains("notary", true) }
        val partyNodeDirs: List<File>
            get() = allSubDirs.filter { !it.name.contains("notary", true) }

        val nodeDirs: List<File>
            get() = notaryNodeDirs + partyNodeDirs

        init {
            FileUtils.copyFile(
                    File(nodeDirs.first(), "network-parameters"),
                    netParamsFile)
        }
    }

    fun startContainers(context: ExtensionContext) {
        if (instances.isEmpty()) {

            val network = Network.newNetwork()
            val userDir = Paths.get(System.getProperty("user.dir")).toAbsolutePath().toFile()
            val fsBindDirs = FsBindDirs(
                    nodesDir = File("/home/manos/git/frag-acs-cordapp/build/nodes")
            )
            fsBindDirs.nodeDirs.forEach { nodeDir ->
                startContainerFromNodedir(fsBindDirs, nodeDir, network)
            }
        }
    }

    private fun startContainerFromNodedir(
            fsBindDirs: FsBindDirs, nodeDir: File, network: Network?
    ) {

        val nodeName = nodeDir.name.decapitalize()
        logger.info("startContainers, node: ${nodeName}")
        val config = ConfigFactory.parseFile(File(nodeDir, "node.conf"))
        //val cordappConf = TypesafeCordappConfig(config)
        val nodeConfig = config.parseAs<NodeConfig>(UnknownConfigKeysPolicy.IGNORE::handle)
        //nodeConfig.p2pAddress.checkPort()
        //nodeConfig.rpcSettings.address.checkPort()
        //nodeConfig.webAddress.checkPort()
        val rpcPort = nodeConfig.rpcSettings.address!!.port
        val exposedPorts = listOf(rpcPort,
                nodeConfig.rpcSettings.adminAddress!!.port,
                nodeConfig.p2pAddress.port)
        val instance = KImageNameContainer(
                DockerImageName.parse("corda/corda-zulu-java1.8-4.5"))
                .withPrivilegedMode(true)
                .withNetworkAliases(nodeName)
                .withNetwork(network)
                .withExposedPorts(*exposedPorts.toTypedArray())
                .withFileSystemBind(
                        nodeDir.absolutePath, "/etc/corda",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.resolve("certificates").absolutePath,
                        "/opt/corda/certificates",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.absolutePath,
                        "/opt/corda/persistence",
                        READ_WRITE)
                .withFileSystemBind(
                        nodeDir.resolve("logs").absolutePath,
                        "/opt/corda/logs",
                        READ_WRITE)
                .withFileSystemBind(
                        fsBindDirs.netParamsFile.absolutePath,
                        "/opt/corda/network-parameters",
                        READ_WRITE)
                .withFileSystemBind(
                        fsBindDirs.nodeInfosDir.absolutePath,
                        "/opt/corda/additional-node-infos",
                        READ_WRITE)

                /*

        -v /home/user/sharedFolder/node-infos:/opt/corda/additional-node-infos \
        -v /home/user/sharedFolder/network-parameters:/opt/corda/network-parameters \
                -v /home/user/cordaBase/config:/etc/corda \
                -v /home/user/cordaBase/certificates:/opt/corda/certificates \
                -v /home/user/cordaBase/persistence:/opt/corda/persistence \
                -v /home/user/cordaBase/logs:/opt/corda/logs \
                -v /path/to/cordapps:/opt/corda/cordapps \
                         */
                //.withEnv("NETWORKMAP_URL", network.map.url)
                //.withEnv("DOORMAN_URL", network.map.url)
                //.withEnv("NETWORK_TRUST_PASSWORD", "trustpass")
                //.withEnv("MY_PUBLIC_ADDRESS", "http://localhost:$p2pPort")
                //.withCommand("config-generator --generic")
                //.withStartupTimeout(timeOut)
                .withCreateContainerCmdModifier { cmd ->
                    cmd.withHostName(nodeName)
                            .withName(nodeName)
                            .withExposedPorts(exposedPorts.map { port ->
                                ExposedPort.tcp(port)
                            })
                }
                .withLogConsumer {
                    logger.info(it.utf8String)
                }
                .waitingFor(Wait.forLogMessage(".*started up and registered in.*", 1))
                .withStartupTimeout(Duration.ofMinutes(2))


        // val setInstance = instance!!
        //setInstance.start()
        /*

             */

        logger.info("starting container: $nodeName")
        instance.start()
        logger.info("started container, \n" +
                "host: ${instance.host}, \n" +
                "address: ${instance.containerIpAddress}, \n" +
                "exposedPorts: ${instance.exposedPorts}, \n" +
                "portBindings: ${instance.portBindings}, \n" +
                "boundPortNumbers: ${instance.boundPortNumbers} \n" +
                "info: ${instance.containerInfo}")

        instances[nodeName] = instance
        if(nodeConfig.notary == null) {
            logger.info("Note non-notary port mapping for : ${nodeName}")
            instancePorts[nodeName] = instance.getMappedPort(rpcPort)
        }
    }

    /**
     * Delegate to [SpringExtension.afterAll],
     * then stop the Corda network
     */
    @Throws(Exception::class)
    override fun afterAll(context: ExtensionContext) {
        super.afterAll(context)
        instances.forEach {
            try{
                 it.value.stop()
            }
            catch (e: Exception){
                logger.warn("Faiuled stopping container", e)
            }
        }

    }

}
