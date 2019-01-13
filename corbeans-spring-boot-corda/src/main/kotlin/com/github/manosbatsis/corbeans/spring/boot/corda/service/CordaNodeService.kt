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
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.utilities.NetworkHostAndPort
import java.io.InputStream
import java.time.LocalDateTime

/**
 *  Basic interface for RPC-based node services
 */
interface CordaNodeService {

    fun getInfo(): NodeInfo = NodeInfo(
            identity = myIdentity,
            identities = identities(),
            platformVersion = platformVersion(),
            peers = peers(),
            peerNames = peerNames(),
            notaries = notaries(),
            flows = flows(),
            addresses = addresses()
        )

    /** Get the node identity */
    val myIdentity: Party

    /** Returns a [CordaRPCOps] proxy for this node. */
    fun proxy(): CordaRPCOps
    /** Returns the node's network peers. */
    fun peers(): List<String>

    /** Returns the (organization) name list of the node's network peers. */
    fun peerNames(): List<String>
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

    fun states(): List<StateAndRef<ContractState>>
    fun flows(): List<String>
    fun notaries(): List<Party>
    fun platformVersion(): Int
    fun identities(): List<Party>
    fun addresses(): List<NetworkHostAndPort>
    fun openArrachment(hash: SecureHash): InputStream
    fun openArrachment(hash: String): InputStream

    /** Get a state service targeting the given `ContractState` type */
    fun <T : ContractState> createStateService(contractStateType: Class<T>): StateService<T>
}
