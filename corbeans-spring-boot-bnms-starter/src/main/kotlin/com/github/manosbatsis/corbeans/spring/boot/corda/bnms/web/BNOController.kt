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

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipPartiesMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.util.getMembership
import com.github.manosbatsis.corbeans.spring.boot.corda.web.CorbeansBaseController
import com.r3.businessnetworks.membership.flows.bno.ActivateMembershipFlow
import com.r3.businessnetworks.membership.flows.bno.SuspendMembershipFlow
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import net.corda.core.messaging.FlowHandle
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = arrayOf("BNMS: Member operations"), description = "BNMS membership operation endpoints")
@RequestMapping(path = arrayOf("api/bnms/bno", "api/bnms/{nodeName}/bno"))
class BNOController : CorbeansBaseController() {

    @PutMapping
    @ApiOperation(value = "Activate a pending membership.")
    fun activateMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): SignedTransaction {
        // Obtain a node service
        val nodeService = getNodeService(nodeName)
        // Load the specified membership state
        val membership = getMembership(
                nodeService.getPartyFromName(input.member),
                nodeService.myIdentity,
                nodeService
        )
        // Activate the membership
        val flowHandle: FlowHandle<SignedTransaction> = nodeService.proxy()
                .startFlowDynamic(ActivateMembershipFlow::class.java, membership)
        return flowHandle.use { it.returnValue.getOrThrow() }
    }

    @DeleteMapping
    @ApiOperation(value = "Suspend an active membership.")
    fun suspendMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): SignedTransaction {
        // Obtain a node service
        val nodeService = getNodeService(nodeName)
        // Load the specified membership state
        val membership = getMembership(
                nodeService.getPartyFromName(input.member),
                nodeService.myIdentity,
                nodeService
        )
        // Activate the membership
        val flowHandle: FlowHandle<SignedTransaction> = nodeService.proxy()
                .startFlowDynamic(SuspendMembershipFlow::class.java, membership)
        return flowHandle.use { it.returnValue.getOrThrow() }
    }

}
