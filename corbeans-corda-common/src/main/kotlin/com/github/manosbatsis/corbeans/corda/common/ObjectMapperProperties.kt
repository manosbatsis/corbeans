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
package com.github.manosbatsis.corbeans.corda.common

import net.corda.core.identity.Party
import net.corda.core.identity.PartyAndCertificate

/**
 * Configuration of the object mapper/Corda Jackson module
 */
class ObjectMapperProperties {

    /** Whether to disable auto-configuration, default is false */
    var disable: Boolean = false

    /**
     * Whether to use an RPC object mapper if properly configured
     */
    var enableRpc: Boolean = false

    /**
     * The [com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService]
     * key to use for obtaining an object mapper RPC proxy.
     * A value of `*` results in a random choice between the available node services.
     * If `null`, a non-RPC object mapper will be used
     */
    var proxyServiceKey: String? = null

    /**
     * Whether to use fuzzy identity matching
     * @see net.corda.client.jackson.JacksonSupport.createDefaultMapper(net.corda.core.messaging.CordaRPCOps, com.fasterxml.jackson.core.JsonFactory, boolean, boolean)
     */
    var fuzzyIdentityMatch: Boolean = false

    /**
     * If true then [Party] objects will be serialised as JSON objects, with the owning key serialised
     * in addition to the name. For [PartyAndCertificate] objects the cert path will be included.
     * @see net.corda.client.jackson.JacksonSupport.createDefaultMapper(net.corda.core.messaging.CordaRPCOps, com.fasterxml.jackson.core.JsonFactory, boolean, boolean)
     */
    var fullParties: Boolean = false

    override fun toString(): String {
        return "ObjectMapperProperties(proxyServiceKey=$proxyServiceKey, fuzzyIdentityMatch=$fuzzyIdentityMatch, fullParties=$fullParties)"
    }


}
