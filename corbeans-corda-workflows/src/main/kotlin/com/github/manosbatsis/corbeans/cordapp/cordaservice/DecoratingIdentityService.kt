package com.github.manosbatsis.corbeans.cordapp.cordaservice

import net.corda.core.contracts.ContractState
import net.corda.core.flows.FlowException
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


    /**
     * Resolve well-known if [AnonymousParty], return as [Party] otherwise
     * @throws [FlowException] when an anonymous party cannot not be resolved
     */
    fun resolve(abstractParty: AbstractParty): Party {
        val party =  if(abstractParty is AnonymousParty) wellKnownPartyFromAnonymous(abstractParty)
            else abstractParty as Party
        return party ?: throw FlowException("Anonymous party could not be resolved")
    }

    /** Resolve participating parties */
    fun resolveParticipants(state: ContractState): List<Party>{
        return state.participants.map {
            if(it is Party) it
            else resolve(it)
        }
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