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
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.web.support.CorbeansBmnsMemberBaseController
import com.r3.businessnetworks.membership.states.MembershipState


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import java.util.Optional

/**
 *  Exposes BNMS Member methods as endpoints.
 *  Supports multiple Corda nodes via a <code>nodeName</code> path variable.
 *  The `nodeName` is used to obtain  an autoconfigured  `CordaNodeService`
 *  for the node configuration matching `nodeName` in application properties.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyBmnsMemberController: CorbeansBmnsMemberPathFragmentController()
 *  ```
 *
 *  @see CorbeansBmnsMemberController
 */
@RequestMapping(path = arrayOf("api/bnms/members/{nodeName}"))
open class CorbeansBmnsMemberPathFragmentController : CorbeansBmnsMemberBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansBmnsMemberPathFragmentController::class.java)
    }

    @PostMapping("memberships")
    @Operation(summary = "Request the BNO to kick-off the on-boarding procedure.")
    override fun createMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage): MembershipState<*> =
            super.createMembershipRequest(nodeName, input)


    @PutMapping("memberships")
    @Operation(summary = "Propose a change to the membership metadata.")
    override fun ammendMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage): MembershipState<*> =
            super.ammendMembershipRequest(nodeName, input)


    @GetMapping("memberships")
    @Operation(summary = "Get a memberships list from a BNO.",
            description = "Members retrieve the full list on the first invocation only. " +
                    "All subsequent updates are delivered via push notifications from the BNO. " +
                    "Memberships cache can be force-refreshed by setting forceRefresh of GetMembershipsFlow to true. " +
                    "Members that are missing from the Network Map are filtered out from the result list.")
    override fun listMemberships(
            @PathVariable
            nodeName: Optional<String>,
            @Parameter(name = "The BNO party name")
            @RequestParam(required = true)
            bno: String,
            @Parameter(name = "The network ID")
            @RequestParam(required = false)
            networkId: Optional<String>,
            @Parameter(name = "Whether to force a refresh.")
            @RequestParam(required = false, defaultValue = "false")
            forceRefresh: Boolean,
            @Parameter(name = "Whether to filter out anyone missing from the Network Map.")
            @RequestParam(required = false, defaultValue = "true")
            filterOutMissingFromNetworkMap: Boolean
    ): List<MembershipState<*>> =
            super.listMemberships(nodeName, bno, networkId, forceRefresh, filterOutMissingFromNetworkMap)



}
