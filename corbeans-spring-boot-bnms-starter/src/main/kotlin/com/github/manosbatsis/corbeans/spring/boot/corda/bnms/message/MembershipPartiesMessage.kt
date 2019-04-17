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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.message

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@ApiModel(description = "A message with the information necessary to identify a membership state.")
open class MembershipPartiesMessage(
        @ApiModelProperty(value = "The member party name") var member: String,
        @ApiModelProperty(value = "The bno party name", required = false) var bno: String? = null
)
