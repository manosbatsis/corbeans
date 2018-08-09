package com.github.manosbatsis.corda.spring.beans

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort
import java.io.InputStream
import java.time.LocalDateTime

/**
 *  Interface for RPC-based node wrapper services
 */
interface CordaNodeService {


    fun states(): List<StateAndRef<ContractState>>
    fun flows(): List<String>
    fun notaries(): List<Party>
    fun platformVersion(): Int
    fun identities(): List<Party>
    fun addresses(): List<NetworkHostAndPort>

    /** Get the node identity */
    fun getMyIdentity(): Party

    /** Returns a list of the node's network peers. */
    fun peers(): Map<String, List<String>>


    /** Returns a list of the node's network peer names. */
    fun peersNames(): Map<String, List<String>>

    fun serverTime(): LocalDateTime
    
    fun openArrachment(hash: SecureHash): InputStream
    fun openArrachment(hash: String): InputStream
}
