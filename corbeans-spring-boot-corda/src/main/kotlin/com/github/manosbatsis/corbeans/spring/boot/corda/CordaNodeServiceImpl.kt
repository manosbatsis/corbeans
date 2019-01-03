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
package com.github.manosbatsis.corbeans.spring.boot.corda

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.node.services.vault.*
import org.apache.commons.io.IOUtils
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.zip.ZipInputStream


/**
 *  Basic RPC-based node service implementation
 */
open class CordaNodeServiceImpl(open val nodeRpcConnection: NodeRpcConnection): CordaNodeService {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNodeServiceImpl::class.java)
    }

    protected val myLegalName: CordaX500Name by lazy {
        myIdentity.name
    }
    override val myIdentity: Party by lazy {
        nodeRpcConnection.proxy.nodeInfo().legalIdentities.first()

    }

    protected val myIdCriteria: QueryCriteria.LinearStateQueryCriteria by lazy {
        QueryCriteria.LinearStateQueryCriteria(participants = listOf(myIdentity))

    }

    val defaultPageSpecification = PageSpecification(pageSize = DEFAULT_PAGE_SIZE, pageNumber = -1)
    var sortByUid = Sort.SortColumn(SortAttribute.Standard(Sort.LinearStateAttribute.UUID), Sort.Direction.DESC)
    var defaultSort = Sort(listOf(sortByUid))


    /** Returns a [CordaRPCOps] proxy for this node. */
    override fun proxy(): CordaRPCOps = this.nodeRpcConnection.proxy

    /** Returns a list of the node's network peers. */
    override fun peers() = mapOf("peers" to nodeRpcConnection.proxy.networkMapSnapshot()
            .filter { nodeInfo -> nodeInfo.legalIdentities.first() != myIdentity }
            .map { it.legalIdentities.first().name.organisation })

    /** Returns a list of the node's network peer names. */
    override fun peerNames(): Map<String, List<String>> {
        val nodes = nodeRpcConnection.proxy.networkMapSnapshot()
        val nodeNames = nodes.map { it.legalIdentities.first().name }
        val filteredNodeNames = nodeNames.filter { it.organisation !== myIdentity.name.organisation }
        val filteredNodeNamesToStr = filteredNodeNames.map { it.toString() }
        return mapOf("peers" to filteredNodeNamesToStr)
    }

    /**
     * Returns a list of candidate matches for a given string, with optional fuzzy(ish) matching. Fuzzy matching may
     * get smarter with time e.g. to correct spelling errors, so you should not hard-code indexes into the results
     * but rather show them via a user interface and let the user pick the one they wanted.
     *
     * @param query The string to check against the X.500 name components
     * @param exactMatch If true, a case sensitive match is done against each component of each X.500 name.
     */
    override fun partiesFromName(query: String, exactMatch: Boolean): Set<Party> =
            this.nodeRpcConnection.proxy.partiesFromName(query, exactMatch)

    override fun serverTime(): LocalDateTime {
        return LocalDateTime.ofInstant(nodeRpcConnection.proxy.currentNodeTime(), ZoneId.of("UTC"))
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