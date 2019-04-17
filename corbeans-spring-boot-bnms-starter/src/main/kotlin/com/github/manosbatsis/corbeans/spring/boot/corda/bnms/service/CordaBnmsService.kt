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

import com.fasterxml.jackson.databind.JsonNode
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipPartiesMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipsListRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
import com.r3.businessnetworks.membership.states.MembershipState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party

/**
 *  Basic interface for Business Network Membership services
 */
interface CordaBnmsService<T : Any> : CordaNodeService {


    // --------------------------
    // Member methods
    // --------------------------

    /** Get the membership matching the given criteria */
    fun getMembership(member: Party, bno: Party): StateAndRef<MembershipState<Any>>?

    /** Request the BNO to kick-off the on-boarding procedure. */
    fun createMembershipRequest(input: MembershipRequestMessage): MembershipState<*>

    /** Propose a change to the membership metadata. */
    fun ammendMembershipRequest(input: MembershipRequestMessage): MembershipState<*>

    /** Get a memberships list from a BNO. */
    fun listMemberships(input: MembershipsListRequestMessage): List<MembershipState<Any>>

    /**
     * Convert the given JSON node to the target `membershipMetadata` instance.
     * By overriding this method you can constructing a metadata instance using the desired type,
     * thus providing a hint for
     * [com.r3.businessnetworks.membership.flows.GenericsUtilsKt#getAttachmentIdForGenericParam(com.r3.businessnetworks.membership.states.MembershipState<? extends java.lang.Object>)]
     * to find the appropriate class location and turn it into an attachment
     */
    fun toMembershipMetadata(meta: JsonNode?): T

    // --------------------------
    // BNO methods
    // --------------------------

    /** Activate a pending membership. */
    fun activateMembership(input: MembershipPartiesMessage): MembershipState<*>

    /** Suspend an active membership.*/
    fun suspendMembership(input: MembershipPartiesMessage): MembershipState<*>

}
