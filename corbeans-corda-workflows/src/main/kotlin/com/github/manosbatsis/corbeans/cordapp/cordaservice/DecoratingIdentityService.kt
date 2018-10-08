/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 2.1 of the License, or (at your option) any later version.
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
package com.github.manosbatsis.corbeans.cordapp.cordaservice

import net.corda.core.identity.*
import net.corda.core.node.AppServiceHub
import net.corda.core.node.services.CordaService
import net.corda.core.node.services.IdentityService
import net.corda.core.serialization.SingletonSerializeAsToken
import net.corda.core.utilities.loggerFor
import java.security.PublicKey
import java.security.cert.CertStore
import java.security.cert.TrustAnchor
import java.security.cert.X509Certificate

@CordaService
class DecoratingIdentityService(
        val serviceHub: AppServiceHub
) : SingletonSerializeAsToken(), IdentityService {


    private companion object {
        val log = loggerFor<DecoratingIdentityService>()
    }

    init {
        log.info("${this.javaClass.simpleName} initialized}")
    }




    override val caCertStore: CertStore = serviceHub.identityService.caCertStore
    override val trustAnchor: TrustAnchor = serviceHub.identityService.trustAnchor
    override val trustRoot: X509Certificate = serviceHub.identityService.trustRoot

    override fun assertOwnership(party: Party, anonymousParty: AnonymousParty) =
        serviceHub.identityService.assertOwnership(party, anonymousParty)

    override fun certificateFromKey(owningKey: PublicKey): PartyAndCertificate? =
        serviceHub.identityService.certificateFromKey(owningKey)

    override fun getAllIdentities(): Iterable<PartyAndCertificate> =
            serviceHub.identityService.getAllIdentities()

    override fun partiesFromName(query: String, exactMatch: Boolean): Set<Party> =
            serviceHub.identityService.partiesFromName(query, exactMatch)

    override fun partyFromKey(key: PublicKey): Party? =
            serviceHub.identityService.partyFromKey(key)

    override fun requireWellKnownPartyFromAnonymous(party: AbstractParty): Party =
            serviceHub.identityService.requireWellKnownPartyFromAnonymous(party)

    override fun verifyAndRegisterIdentity(identity: PartyAndCertificate): PartyAndCertificate? =
            serviceHub.identityService.verifyAndRegisterIdentity(identity)

    override fun wellKnownPartyFromAnonymous(party: AbstractParty): Party?=
            serviceHub.identityService.wellKnownPartyFromAnonymous(party)

    override fun wellKnownPartyFromX500Name(name: CordaX500Name): Party? =
            serviceHub.identityService.wellKnownPartyFromX500Name(name)

}