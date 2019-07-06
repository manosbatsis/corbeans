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
import io.swagger.annotations.ApiOperation
import org.springframework.web.bind.annotation.*
import java.util.*

/**
 *  Exposes BNMS Operator (BNO) methods as endpoints.
 *  Supports multiple Corda nodes via a <code>nodeName</code> path variable.
 *  The `nodeName` is used to obtain  an autoconfigured  `CordaNodeService`
 *  for the node configuration matching `nodeName` in application properties.
 *
 *  To use the controller simply extend it and add a `@RestController` annotation:
 *
 *  ```
 *  @RestController
 *  class MyBmnsBnoController: CorbeansBmnsBnoPathFragmentController()
 *  ```
 *
 *  @see CorbeansBmnsBnoPathFragmentController
 */
@RequestMapping(path = arrayOf("api/bnms/bnos/{nodeName}"))
open class CorbeansBmnsBnoPathFragmentController : CorbeansBmnsBnoBaseController() {

    @PutMapping("memberships")
    @ApiOperation(value = "Activate a pending membership.")
    override fun activateMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            super.activateMembership(nodeName, input)

    @DeleteMapping("memberships")
    @ApiOperation(value = "Suspend an active membership.")
    override fun suspendMembership(
            @PathVariable nodeName: Optional<String>,
            @RequestBody input: MembershipPartiesMessage): MembershipState<*> =
            super.suspendMembership(nodeName, input)

}
