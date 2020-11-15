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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.service

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipPartiesMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipsListRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.util.getMembership
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaRpcServiceBase
import com.github.manosbatsis.vaultaire.service.node.NodeServiceRpcPoolBoyDelegate
import net.corda.bn.flows.ActivateMembershipFlow
import net.corda.bn.flows.MembershipRequest
import net.corda.bn.flows.RequestMembershipFlow
import net.corda.bn.flows.SuspendMembershipFlow
import net.corda.bn.states.BNIdentity
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory


/**
 *  Basic BNMS service implementation
 */
abstract class CordaBnmsServiceBase<T : BNIdentity>(
        override val delegate: NodeServiceRpcPoolBoyDelegate
) : CordaRpcServiceBase(delegate), CordaBnmsService<T> {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaBnmsServiceBase::class.java)
    }

    /** Get the membership matching the given criteria */
    override fun getMembership(member: Party, bno: Party): StateAndRef<MembershipState>? =
            getMembership(member, bno, this)

    /** Request the BNO to kick-off the on-boarding procedure. */
    override fun createMembershipRequest(input: MembershipRequestMessage<T>): MembershipState =
            createMembershipRequest(
                    getPartyFromName(input.authorisedParty.toString()),
                    MembershipRequest(
                            networkId = input.networkId,
                            businessIdentity = input.businessIdentity,
                            notary = input.notary?.run { getPartyFromName(this.toString()) }))

    /** Request the BNO to kick-off the on-boarding procedure. */
    override fun createMembershipRequest(
            authorisedParty: Party,
            membershipRequest: MembershipRequest
    ): MembershipState {
        return delegate.poolBoy.withConnection { connection ->

            val flowHandle: FlowHandle<SignedTransaction> =
                    connection.proxy.startFlowDynamic(
                            RequestMembershipFlow::class.java,
                            authorisedParty,
                            membershipRequest.networkId,
                            membershipRequest.businessIdentity,
                            membershipRequest.notary)
             flowHandle.use { it.returnValue.getOrThrow() }
                    .tx.outputStates.single() as MembershipState
        }
    }


    /**
     * Get a memberships list from a BNO
     * @param bno The BNO party name
     * @param forceRefresh Whether to force a refresh.
     * @param filterOutMissingFromNetworkMap Whether to filter out anyone missing from the Network Map.
     */
    override fun listMemberships(input: MembershipsListRequestMessage): List<MembershipState> =
            listMemberships(
                    getPartyFromName(input.bno),
                    input.networkId,
                    input.forceRefresh,
                    input.filterOutMissingFromNetworkMap)

    /**
     * Get a memberships list from a BNO
     * @param bno The BNO party
     * @param forceRefresh Whether to force a refresh.
     * @param filterOutMissingFromNetworkMap Whether to filter out anyone missing from the Network Map.
     */
    override fun listMemberships(
            bno: Party,
            networkID: String?,
            forceRefresh: Boolean,
            filterOutMissingFromNetworkMap: Boolean
    ): List<MembershipState> {
        return delegate.poolBoy.withConnection { connection ->
            // Load memberships
            val flowHandle: FlowHandle<Map<Party, StateAndRef<MembershipState<Any>>>> =
                    connection.proxy.startFlowDynamic(
                            GetMembershipsFlow::class.java,
                            bno,
                            networkID,
                            forceRefresh,
                            filterOutMissingFromNetworkMap)

            // Map to a list and return
            flowHandle.use { it.returnValue.getOrThrow() }
                    .values.map { it.state.data as MembershipState }
        }
    }

    /** Activate a pending membership. */
    override fun activateMembership(input: MembershipPartiesMessage): MembershipState =
            activateMembership(
                    getPartyFromName(input.member),
                    if (input.bno != null) getPartyFromName(input.bno!!) else myIdentity)

    /** Activate a pending membership. */
    override fun activateMembership(member: Party, bno: Party): MembershipState {

        return delegate.poolBoy.withConnection { connection ->
            // Load the specified membership state
            val membership = getMembership(member, bno)
            logger.debug("activateMembership, membership: $membership")
            // Activate the membership and return
            val flowHandle: FlowHandle<SignedTransaction> =
                    connection.proxy.startFlowDynamic(ActivateMembershipFlow::class.java, membership)
            val tx = flowHandle.use { it.returnValue.getOrThrow() }
            tx.tx.outputStates.single() as MembershipState
        }
    }

    /** Suspend an active membership.*/
    override fun suspendMembership(input: MembershipPartiesMessage): MembershipState =
            suspendMembership(
                    getPartyFromName(input.member),
                    if (input.bno != null) getPartyFromName(input.bno!!) else myIdentity)

    /** Suspend an active membership.*/
    override fun suspendMembership(member: Party, bno: Party): MembershipState {
        return delegate.poolBoy.withConnection { connection ->
            // Load the specified membership state
            val membership = getMembership(member, bno)
            // Suspend the membership and return
            val flowHandle: FlowHandle<SignedTransaction> =
                    connection.proxy.startFlowDynamic(SuspendMembershipFlow::class.java, membership)
            val tx = flowHandle.use { it.returnValue.getOrThrow() }
            tx.tx.outputStates.single() as MembershipState
        }
    }

}
