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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.JsonNodeDeserializer
import io.swagger.v3.oas.annotations.media.Schema
import net.corda.bn.states.BNIdentity
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

/**
 * A flexible membership request message that allows arbitrary membership metadata to be used
 * in combination with a custom
 */
@CordaSerializable
@Schema(description = "A message with the information necessary to create or ammend a membership request.")
class MembershipRequestMessage<T: BNIdentity>(
        @Schema(title = "The authorised party name", required = true)
        val authorisedParty: CordaX500Name,
        @Schema(title = "The network ID", required = true)
        val networkId: String,
        @Schema(title = "The membership identity", required = false)
        var businessIdentity: T? = null,
        @Schema(title = "The notary to use for the request", required = false)
        var notary: CordaX500Name? = null
)
