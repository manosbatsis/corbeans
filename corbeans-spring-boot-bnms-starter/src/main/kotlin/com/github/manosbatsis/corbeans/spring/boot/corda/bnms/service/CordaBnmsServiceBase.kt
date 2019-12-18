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

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipPartiesMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipsListRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.util.getMembership
import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaRpcServiceBase
import com.r3.businessnetworks.membership.flows.bno.ActivateMembershipFlow
import com.r3.businessnetworks.membership.flows.bno.SuspendMembershipFlow
import com.r3.businessnetworks.membership.flows.member.AmendMembershipMetadataFlow
import com.r3.businessnetworks.membership.flows.member.GetMembershipsFlow
import com.r3.businessnetworks.membership.flows.member.RequestMembershipFlow
import com.r3.businessnetworks.membership.states.MembershipState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.slf4j.LoggerFactory


/**
 *  Basic BNMS service implementation, extend to create your own.
 *  You will need to register your custom implementation in _application.properties_ e.g.
 *
 *  ```
 *  corbeans.nodes.myParty.bnmsServiceType=my.custom.CordaBnmsServiceImpl
 *  ```
 *
 *  for Corbeans to create and register the corresponding service beans for you.
 */
abstract class CordaBnmsServiceBase<T : Any>(
        nodeRpcConnection: NodeRpcConnection
) : CordaRpcServiceBase(nodeRpcConnection), CordaBnmsService<T> {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaBnmsServiceBase::class.java)
    }

    /** Get the membership matching the given criteria */
    override fun getMembership(member: Party, bno: Party): StateAndRef<MembershipState<T>>? =
            getMembership(member, bno, this)

    /** Request the BNO to kick-off the on-boarding procedure. */
    override fun createMembershipRequest(input: MembershipRequestMessage): MembershipState<T> =
            createMembershipRequest(
                    getPartyFromName(input.party),
                    toMembershipMetadata(input.membershipMetadata),
                    input.networkId)

    /** Request the BNO to kick-off the on-boarding procedure. */
    override fun createMembershipRequest(bno: Party, membershipMetadata: T, networkId: String?): MembershipState<T> {
        // Create the state and return
        val flowHandle: FlowHandle<SignedTransaction> = proxy()
                .startFlowDynamic(RequestMembershipFlow::class.java, bno, membershipMetadata, networkId)
        return flowHandle.use { it.returnValue.getOrThrow() }
                .tx.outputStates.single() as MembershipState<T>
    }

    /** Propose a change to the membership metadata. */
    override fun ammendMembershipRequest(input: MembershipRequestMessage): MembershipState<T> =
            ammendMembershipRequest(
                    getPartyFromName(input.party),
                    toMembershipMetadata(input.membershipMetadata),
                    input.networkId)

    /** Propose a change to the membership metadata. */
    override fun ammendMembershipRequest(bno: Party, membershipMetadata: T, networkId: String?): MembershipState<T> {
        // Create the state
        val flowHandle: FlowHandle<SignedTransaction> = proxy()
                .startFlowDynamic(AmendMembershipMetadataFlow::class.java, bno, membershipMetadata)
        val tx = flowHandle.use { it.returnValue.getOrThrow() }
        return tx.tx.outputStates.single() as MembershipState<T>
    }

    /**
     * Get a memberships list from a BNO
     * @param bno The BNO party name
     * @param forceRefresh Whether to force a refresh.
     * @param filterOutMissingFromNetworkMap Whether to filter out anyone missing from the Network Map.
     */
    override fun listMemberships(input: MembershipsListRequestMessage): List<MembershipState<T>> =
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
            filterOutMissingFromNetworkMap: Boolean): List<MembershipState<T>> {
        proxy()
        // Load memberships
        val flowHandle: FlowHandle<Map<Party, StateAndRef<MembershipState<Any>>>> = proxy()
                .startFlowDynamic(
                        GetMembershipsFlow::class.java,
                        bno,
                        networkID,
                        forceRefresh,
                        filterOutMissingFromNetworkMap)

        // Map to a list and return
        return flowHandle.use { it.returnValue.getOrThrow() }
                .values.map { it.state.data as MembershipState<T>}
    }

    /** Activate a pending membership. */
    override fun activateMembership(input: MembershipPartiesMessage): MembershipState<T> =
            activateMembership(
                    getPartyFromName(input.member),
                    if(input.bno != null) getPartyFromName(input.bno!!) else myIdentity)

    /** Activate a pending membership. */
    override fun activateMembership(member: Party, bno: Party): MembershipState<T> {
        // Load the specified membership state
        val membership = getMembership(member, bno)
        logger.debug("activateMembership, membership: $membership")
        // Activate the membership and return
        val flowHandle: FlowHandle<SignedTransaction> = proxy()
                .startFlowDynamic(ActivateMembershipFlow::class.java, membership)
        val tx = flowHandle.use { it.returnValue.getOrThrow() }
        return tx.tx.outputStates.single() as MembershipState<T>
    }

    /** Suspend an active membership.*/
    override fun suspendMembership(input: MembershipPartiesMessage): MembershipState<T> =
            suspendMembership(
                    getPartyFromName(input.member),
                    if(input.bno != null) getPartyFromName(input.bno!!) else myIdentity)

    /** Suspend an active membership.*/
    override fun suspendMembership(member: Party, bno: Party): MembershipState<T> {
        // Load the specified membership state
        val membership = getMembership(member, bno)
        // Suspend the membership and return
        val flowHandle: FlowHandle<SignedTransaction> = proxy()
                .startFlowDynamic(SuspendMembershipFlow::class.java, membership)
        val tx = flowHandle.use { it.returnValue.getOrThrow() }
        return tx.tx.outputStates.single() as MembershipState<T>
    }

}
