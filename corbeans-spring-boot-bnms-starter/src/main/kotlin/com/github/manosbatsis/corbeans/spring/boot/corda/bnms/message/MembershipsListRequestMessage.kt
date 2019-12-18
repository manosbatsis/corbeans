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
@ApiModel(description = "A message with the information necessary to obtain the members list.")
open class MembershipsListRequestMessage(
        @ApiModelProperty(value = "The BNO party name")
        var bno: String,
        @ApiModelProperty(value = "The network ID")
        var networkId: String? = null,
        @ApiModelProperty(value = "Wether to force a refresh.")
        var forceRefresh: Boolean = false,
        @ApiModelProperty(value = "Wether to filter out anyone missing from the Network Map.")
        var filterOutMissingFromNetworkMap: Boolean = true
)
