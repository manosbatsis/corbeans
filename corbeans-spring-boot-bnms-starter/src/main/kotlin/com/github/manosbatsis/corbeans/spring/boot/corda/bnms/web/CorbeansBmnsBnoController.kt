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
import com.r3.businessnetworks.membership.states.MembershipState
import io.swagger.annotations.Api
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@Api(tags = arrayOf("BNMS BNO"), description = "BNO operation endpoints")
@RequestMapping(path = arrayOf("api/bnms/bno", "api/bnms/bnos/{nodeName}"))
class CorbeansBmnsBnoController : CorbeansBmnsBaseController() {

    @PutMapping("memberships")
    @ApiOperation(value = "Activate a pending membership.")
    fun activateMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            getBmnsService(nodeName).activateMembership(input)

    @DeleteMapping("memberships")
    @ApiOperation(value = "Suspend an active membership.")
    fun suspendMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            getBmnsService(nodeName).suspendMembership(input)

}
