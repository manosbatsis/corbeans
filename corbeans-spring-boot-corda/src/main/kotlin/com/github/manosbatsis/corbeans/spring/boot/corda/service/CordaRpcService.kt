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

import com.github.manosbatsis.corbeans.spring.boot.corda.model.info.NodeInfo
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.Attachment
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentFile
import com.github.manosbatsis.corbeans.spring.boot.corda.model.upload.AttachmentReceipt
import com.github.manosbatsis.vaultaire.service.dao.StateService
import net.corda.core.contracts.ContractState
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import java.io.InputStream
import java.time.LocalDateTime

/**
 *  Basic interface for RPC-based node services
 */
interface CordaRpcService {

    fun getInfo(): NodeInfo = NodeInfo(
            identity = myIdentity,
            identities = identities(),
            platformVersion = platformVersion(),
            notaries = notaries(),
            flows = flows(),
            addresses = addresses()
        )

    /** Get the node identity */
    val myIdentity: Party

    /** Returns a [CordaRPCOps] proxy for this node. */
    fun proxy(): CordaRPCOps

    /** Get a list of nodes in the network, including self and notaries. */
    fun nodes(): List<Party>

    /** Returns the node's network peers, excluding self and notaries. */
    fun peers(): List<Party>

    /** Refreshes the node's NetworkMap cache */
    fun refreshNetworkMapCache(): Unit
    
    /**
     * Returns a list of candidate matches for a given string, with optional fuzzy(ish) matching. Fuzzy matching may
     * get smarter with time e.g. to correct spelling errors, so you should not hard-code indexes into the results
     * but rather show them via a user interface and let the user pick the one they wanted.
     *
     * @param query The string to check against the X.500 name components
     * @param exactMatch If true, a case sensitive match is done against each component of each X.500 name.
     */
    fun partiesFromName(query: String, exactMatch: Boolean = false): Set<Party>
    fun serverTime(): LocalDateTime
    fun flows(): List<String>
    fun notaries(): List<Party>
    fun platformVersion(): Int
    fun identities(): List<Party>
    fun addresses(): List<NetworkHostAndPort>
    /** Retrieve the attachment matching the given secure hash from the vault  */
    fun openAttachment(hash: SecureHash): InputStream
    /** Retrieve the attachment matching the given hash string from the vault  */
    fun openAttachment(hash: String): InputStream
    /** Persist the given file as an attachment in the vault  */
    fun saveAttachment(attachmentFile: AttachmentFile): AttachmentReceipt
    /** Persist the given files as a single attachment in the vault  */
    fun saveAttachment(attachmentFiles: List<AttachmentFile>): AttachmentReceipt
    /** Persist the given attachment to the vault  */
    fun saveAttachment(attachment: Attachment): AttachmentReceipt
    /** Get a state service targeting the given `ContractState` type */
    fun <T : ContractState> createStateService(contractStateType: Class<T>): StateService<T>
    /** Whether this service should be skipped from actuator */
    fun skipInfo(): Boolean

    fun findPartyFromName(query: String): Party?
    fun getPartyFromName(query: String): Party
}
