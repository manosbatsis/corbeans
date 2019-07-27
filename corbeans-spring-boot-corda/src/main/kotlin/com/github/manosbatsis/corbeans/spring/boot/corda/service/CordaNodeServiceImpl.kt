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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.Attachment
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentFile
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentReceipt
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.toAttachment
import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.node.services.vault.QueryCriteria
import org.slf4j.LoggerFactory
import java.io.InputStream
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*


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


    /** Returns a [CordaRPCOps] proxy for this node. */
    override fun proxy(): CordaRPCOps = this.nodeRpcConnection.proxy


    /** Get a list of nodes in the network, including self and notaries */
    override fun nodes(): List<Party> {
        val nodes = nodeRpcConnection.proxy.networkMapSnapshot()
        return nodes.map { it.legalIdentities.first() }

    }

    /** Get a list of network peers, i.e. nodes excluding self and notaries  */
    override fun peers(): List<Party> {
        val notaries = this.notaries()
        val nodes = nodeRpcConnection.proxy.networkMapSnapshot()
        return nodes.filter { nodeInfo ->
            // Filter out self and notaries
            nodeInfo.legalIdentities.find {
                it == myIdentity || notaries.contains(it)
            } == null
        }
                .map { it.legalIdentities.first() }
    }


    /**
     * Returns a [Party] match for the given name string, trying exact and if needed fuzzy matching.
     * @param name The name to convert to a party
     */
    override fun findPartyFromName(query: String): Party? =
            this.partiesFromName(query, true).firstOrNull()
                    ?: this.partiesFromName(query, true).firstOrNull()


    /**
     * Returns a [Party] match for the given name string, trying exact and if needed fuzzy matching.
     * If not exactly one match is found an error will be thrown.
     * @param name The name to convert to a party
     */
    override fun getPartyFromName(query: String): Party =
            if(query.contains("O=")) this.nodeRpcConnection.proxy.wellKnownPartyFromX500Name(CordaX500Name.parse(query))
                    ?: throw IllegalArgumentException("No party found for query treated as an x500 name: ${query}")
            else this.partiesFromName(query, true).firstOrNull()
                    ?: this.partiesFromName(query, false).single()


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

    /** Get a state service targeting the given `ContractState` type */
    override fun <T : ContractState> createStateService(contractStateType: Class<T>): StateService<T>{
        return StateService(contractStateType, this)
    }

    /** Retrieve the attachment matching the given hash string from the vault  */
    override fun openAttachment(hash: String): InputStream = this.openAttachment(SecureHash.parse(hash))
    /** Retrieve the attachment matching the given secure hash from the vault  */
    override fun openAttachment(hash: SecureHash): InputStream = nodeRpcConnection.proxy.openAttachment(hash)

    /** Persist the given file(s) as a single attachment in the vault  */
    override fun saveAttachment(attachmentFile: AttachmentFile): AttachmentReceipt {
        return this.saveAttachment(listOf(attachmentFile))
    }

    /** Persist the given file(s) as a single attachment in the vault  */
    override fun saveAttachment(attachmentFiles: List<AttachmentFile>): AttachmentReceipt {
        return this.saveAttachment(toAttachment(attachmentFiles))
    }

    /** Persist the given attachment to the vault  */
    override fun saveAttachment(attachment: Attachment): AttachmentReceipt {
        // Upload to vault
        val hash = attachment.use {
            it.inputStream.use {
                this.proxy().uploadAttachment(it).toString()
            }
        }
        // Return receipt
        return AttachmentReceipt(
                date = Date(),
                hash = hash,
                files = attachment.filenames,
                author = this.myIdentity.name.organisation,
                savedOriginal = attachment.original
        )
    }

    /** Whether this service should be skipped from actuator */
    override fun skipInfo(): Boolean = this.nodeRpcConnection.skipInfo()
}
