/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
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
package com.github.manosbatsis.corda.spring.beans

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corda.spring.beans.util.NodeRpcConnection
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.messaging.vaultQueryBy
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.util.zip.ZipInputStream


/**
 *  Basic primary RPC-based node service implementation
 */
open class CordaNodeServiceImpl(override val nodeRpcConnection: NodeRpcConnection):
        BaseCordaNodeServiceImpl(nodeRpcConnection), CordaNodeService {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodeServiceImpl::class.java)
    }

    override fun addresses() = nodeRpcConnection.proxy.nodeInfo().addresses

    override fun identities() = nodeRpcConnection.proxy.nodeInfo().legalIdentities

    override fun platformVersion() = nodeRpcConnection.proxy.nodeInfo().platformVersion

    override fun notaries() = nodeRpcConnection.proxy.notaryIdentities()

    override fun flows() = nodeRpcConnection.proxy.registeredFlows()

    override fun states() = nodeRpcConnection.proxy.vaultQueryBy<ContractState>().states


    override fun openArrachment(hash: String): InputStream = this.openArrachment(SecureHash.parse(hash))
    override fun openArrachment(hash: SecureHash): InputStream = nodeRpcConnection.proxy.openAttachment(hash)

    @Throws(IOException::class)
    private fun convertToInputStream(inputStreamIn: ZipInputStream): InputStream {
        val out = ByteArrayOutputStream()
        IOUtils.copy(inputStreamIn, out)
        return ByteArrayInputStream(out.toByteArray())
    }

}