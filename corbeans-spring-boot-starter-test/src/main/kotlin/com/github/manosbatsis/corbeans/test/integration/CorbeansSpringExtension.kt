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
package com.github.manosbatsis.corbeans.test.integration

import net.corda.core.serialization.internal.AttachmentURLStreamHandlerFactory
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace
import org.junit.jupiter.api.extension.ExtensionContext.Store.CloseableResource
import org.slf4j.LoggerFactory
import org.springframework.test.context.junit.jupiter.SpringExtension
import java.net.URL


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
class CorbeansSpringExtension: SpringExtension() {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansSpringExtension::class.java)
    }

    private var ownsNetwork = false

    /**
     * Delegate to [SpringExtension.beforeAll],
     * then start the Corda network
     */
    @Throws(Exception::class)
    override fun beforeAll(context: ExtensionContext) {
        context.getStore(Namespace.create(CorbeansSpringExtension::class))
                .getOrComputeIfAbsent("cordaNetwork", {key -> startNodes(context)})
        logger.debug("Network is up, starting Spring container")
        super.beforeAll(context)
    }

    /**
     * Fixes a linkage error when the decorated URLStreamHandlerFactory from
     * https://github.com/corda/corda/blob/879c3c72e6386069c2c9296d0b7d9b9316e2b588/core/src/main/kotlin/net/corda/core/serialization/internal/AttachmentsClassLoader.kt#L154
     * is used, resulting in:
     * ```
     * java.lang.LinkageError: net/corda/core/serialization/internal/AttachmentURLStreamHandlerFactory
     *   at net.corda.core.serialization.internal.AttachmentsClassLoader.<init>(AttachmentsClassLoader.kt:454) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.serialization.internal.AttachmentsClassLoaderBuilder$withAttachmentsClassloaderContext$serializationContext$1.apply(AttachmentsClassLoader.kt:317) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.serialization.internal.AttachmentsClassLoaderBuilder$withAttachmentsClassloaderContext$serializationContext$1.apply(AttachmentsClassLoader.kt:290) ~[corda-core-4.5-RC05.jar:?]
     *   at java.util.HashMap.computeIfAbsent(HashMap.java:1127) ~[?:1.8.0_181]
     *   at java.util.Collections$SynchronizedMap.computeIfAbsent(Collections.java:2672) ~[?:1.8.0_181]
     *   at net.corda.core.serialization.internal.AttachmentsClassLoaderBuilder.withAttachmentsClassloaderContext(AttachmentsClassLoader.kt:315) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.serialization.internal.AttachmentsClassLoaderBuilder.withAttachmentsClassloaderContext$default(AttachmentsClassLoader.kt:311) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.LedgerTransaction.internalPrepareVerify$core(LedgerTransaction.kt:217) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.LedgerTransaction.verify(LedgerTransaction.kt:207) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.TransactionBuilder.addMissingDependency(TransactionBuilder.kt:206) ~[corda-core-4.5-RC05.jar:?]\
     *   at net.corda.core.transactions.TransactionBuilder.toWireTransactionWithContext(TransactionBuilder.kt:186) ~[corda-core-4.5-RC05.jar:?]\\
     *   at net.corda.core.transactions.TransactionBuilder.toWireTransactionWithContext$core(TransactionBuilder.kt:146) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.TransactionBuilder.toWireTransaction(TransactionBuilder.kt:140) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.TransactionBuilder.toLedgerTransaction(TransactionBuilder.kt:622) ~[corda-core-4.5-RC05.jar:?]
     *   at net.corda.core.transactions.TransactionBuilder.verify(TransactionBuilder.kt:630) ~[corda-core-4.5-RC05.jar:?]
     * ```
     */
    override fun postProcessTestInstance(testInstance: Any, context: ExtensionContext) {
        super.postProcessTestInstance(testInstance, context)
        val lockField = URL::class.java.getDeclaredField("streamHandlerLock")
        // It is a private field so we need to make it accessible
        // Note: this will only work as-is in JDK8.
        lockField.isAccessible = true
        // Use the same lock to reset the factory
        synchronized(lockField.get(null)) {
            // Retrieve the `URL.factory` field
            val factoryField = URL::class.java.getDeclaredField("factory")
            // Make it accessible
            factoryField.isAccessible = true
            // Reset the value to prevent Error due to a factory already defined
            factoryField.set(null, null)
            // Set our custom factory and wrap the current one into it
            URL.setURLStreamHandlerFactory(AttachmentURLStreamHandlerFactory/*
                    // Set the factory to a decorator
                    object : URLStreamHandlerFactory {
                        // route between our own and the pre-existing factory
                        override fun createURLStreamHandler(protocol: String): URLStreamHandler? {
                            return AttachmentURLStreamHandlerFactory.createURLStreamHandler(protocol)
                                    ?: existingFactory.createURLStreamHandler(protocol)
                        }
                    }
                    */
            )
        }
    }

    fun startNodes(context: ExtensionContext): CordaNetwork{
        ownsNetwork = true
        return CordaNetwork()
    }

    /**
     * Delegate to [SpringExtension.afterAll],
     * then stop the Corda network
     */
    @Throws(Exception::class)
    override fun afterAll(context: ExtensionContext) {
        // Is network owner?
        if(ownsNetwork){
            // Remove/stop network if existing
            val cordaNetwork: CordaNetwork? = context.getStore(Namespace.create(CorbeansSpringExtension::class))
                    .remove("cordaNetwork", CordaNetwork::class.java)
            if(cordaNetwork != null) cordaNetwork.close()
        }
        logger.debug("Stopping Spring container...")
        super.afterAll(context)
    }

    class CordaNetwork : CloseableResource {

        val nodeDriverHelper: NodeDriverHelper

        init {
            logger.debug("Starting Corda network")
            // Start the network
            this.nodeDriverHelper = NodeDriverHelper()
            this.nodeDriverHelper.startNetwork()
        }

        override fun close() {
            logger.debug("Stopping Corda network...")
            this.nodeDriverHelper.stopNetwork()
        }

    }
}
