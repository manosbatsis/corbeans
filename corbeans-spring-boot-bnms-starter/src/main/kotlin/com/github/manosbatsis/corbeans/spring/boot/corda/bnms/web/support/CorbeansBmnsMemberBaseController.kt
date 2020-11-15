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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.web.support

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipRequestMessage
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message.MembershipsListRequestMessage
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import net.corda.bn.states.BNIdentity
import net.corda.bn.states.MembershipState

import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import java.util.Optional


@Tag(name = "BNMS Member", description = "BNMS membership operation endpoints")
open class CorbeansBmnsMemberBaseController<T: BNIdentity> : CorbeansBmnsBaseController<T>() {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansBmnsMemberBaseController::class.java)
    }

    open fun createMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage<T>): MembershipState =
            this.getBmnsService(nodeName).createMembershipRequest(input)


    open fun ammendMembershipRequest(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipRequestMessage<T>): MembershipState =
            this.getBmnsService(nodeName).ammendMembershipRequest(input)


    open fun listMemberships(
            @PathVariable
            nodeName: Optional<String>,
            @Parameter(name = "The BNO party name")
            @RequestParam(required = true)
            bno: String,
            @Parameter(name = "The network ID")
            @RequestParam(required = false)
            networkId: Optional<String>,
            @Parameter(name = "Wether to force a refresh.")
            @RequestParam(required = false, defaultValue = "false")
            forceRefresh: Boolean,
            @Parameter(name = "Wether to filter out anyone missing from the Network Map.")
            @RequestParam(required = false, defaultValue = "true")
            filterOutMissingFromNetworkMap: Boolean
    ): List<MembershipState> =
            this.getBmnsService(nodeName)
                    .listMemberships(
                            MembershipsListRequestMessage(bno, networkId.orElse(null), forceRefresh, filterOutMissingFromNetworkMap))


}
