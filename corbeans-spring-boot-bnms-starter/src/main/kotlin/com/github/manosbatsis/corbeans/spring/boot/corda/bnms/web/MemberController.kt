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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.web

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipsListRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.web.CorbeansBaseController
import com.r3.businessnetworks.membership.flows.member.AmendMembershipMetadataFlow
import com.r3.businessnetworks.membership.flows.member.GetMembershipsFlow
import com.r3.businessnetworks.membership.flows.member.RequestMembershipFlow
import com.r3.businessnetworks.membership.states.MembershipState
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = arrayOf("BNMS: Member operations"), description = "BNMS membership operation endpoints")
@RequestMapping(path = arrayOf("api/bnms/memberships", "api/bnms/{nodeName}/memberships"))
class MemberController : CorbeansBaseController() {

    @PostMapping
    @ApiOperation(value = "Requests the BNO to kick-off the on-boarding procedure.")
    fun createMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage): SignedTransaction {
        val nodeService = getNodeService(nodeName)
        val flowHandle: FlowHandle<SignedTransaction> = nodeService.proxy()
                .startFlowDynamic(
                        RequestMembershipFlow::class.java,
                        nodeService.getPartyFromName(input.party),
                        input.membershipMetadata)
        return flowHandle.use { it.returnValue.getOrThrow() }
    }

    @PutMapping
    @ApiOperation(value = "Propose a change to the membership metadata.")
    fun ammendMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage): SignedTransaction {
        val nodeService = getNodeService(nodeName)
        val flowHandle: FlowHandle<SignedTransaction> = nodeService.proxy()
                .startFlowDynamic(
                        AmendMembershipMetadataFlow::class.java,
                        nodeService.getPartyFromName(input.party),
                        input.membershipMetadata)
        return flowHandle.use { it.returnValue.getOrThrow() }
    }

    @GetMapping
    @ApiOperation(value = "Get a memberships list from a BNO.",
            notes = "Members retrieve the full list on the first invocation only. " +
                    "All subsequent updates are delivered via push notifications from the BNO. " +
                    "Memberships cache can be force-refreshed by setting forceRefresh of GetMembershipsFlow to true. " +
                    "Members that are missing from the Network Map are filtered out from the result list.")
    fun listMemberships(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipsListRequestMessage
    ): Map<Party, StateAndRef<MembershipState<Any>>> {
        val nodeService = getNodeService(nodeName)
        val flowHandle: FlowHandle<Map<Party, StateAndRef<MembershipState<Any>>>> = nodeService.proxy()
                .startFlowDynamic(
                        GetMembershipsFlow::class.java,
                        nodeService.getPartyFromName(input.party),
                        input.forceRefresh,
                        input.filterOutMissingFromNetworkMap)
        return flowHandle.use { it.returnValue.getOrThrow() }
    }
}
