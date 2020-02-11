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
import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.web.support.CorbeansBmnsBnoBaseController
import com.r3.businessnetworks.membership.states.MembershipState
import io.swagger.v3.oas.annotations.Operation
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import java.util.Optional

/**
 *  Exposes BNMS Operator (BNO) methods as endpoints for a single node or,
 *  if by overriding `getRequestNodeName()`, multiple nodes.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyBmnsBnoController: CorbeansBmnsBnoController()
 *  ```
 *
 *  @see CorbeansBmnsBnoPathFragmentController
 */
@RequestMapping(path = arrayOf("api/bnms/bno"))
open class CorbeansBmnsBnoController : CorbeansBmnsBnoBaseController() {

    /** Override to control how the the node name is resolved based on the request by e.g. parsing headers */
    open fun getRequestNodeName(): Optional<String> = Optional.empty()

    @PutMapping("memberships")
    @Operation(summary = "Activate a pending membership.")
    fun activateMembership(@RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            super.activateMembership(getRequestNodeName(), input)

    @DeleteMapping("memberships")
    @Operation(summary = "Suspend an active membership.")
    fun suspendMembership(@RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            super.suspendMembership(getRequestNodeName(), input)

}
