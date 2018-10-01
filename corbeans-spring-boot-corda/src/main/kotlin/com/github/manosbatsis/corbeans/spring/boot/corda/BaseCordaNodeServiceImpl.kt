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
package com.github.manosbatsis.corbeans.spring.boot.corda

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.*
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.ZoneId


/**
 *  Basic RPC-based node service implementation
 */
open class BaseCordaNodeServiceImpl(open val nodeRpcConnection: NodeRpcConnection): BaseCordaNodeService {

    companion object {
        private val logger = LoggerFactory.getLogger(BaseCordaNodeServiceImpl::class.java)
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

    override fun serverTime(): LocalDateTime {
        return LocalDateTime.ofInstant(nodeRpcConnection.proxy.currentNodeTime(), ZoneId.of("UTC"))
    }


}