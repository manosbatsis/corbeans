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
package com.github.manosbatsis.corbeans.cordapp.extention

import net.corda.core.contracts.ContractState
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.AnonymousParty
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub


/** Get the first notary found in the network map */
fun ServiceHub.getFirstNotary(): Party = networkMapCache.notaryIdentities.firstOrNull()
        ?: throw RuntimeException("No notaries found in network map cache")

/** Get the first notary matching the given organisation name*/
@Suppress("unused")
fun ServiceHub.getNotaryByOrganisation(organisation: String) : Party =
        networkMapCache.notaryIdentities.firstOrNull { it.name.organisation == organisation }
                ?: throw RuntimeException("No notaries found in network map cache for organisation $organisation")

/**
 * Resolve well-known if [AnonymousParty], return as [Party] otherwise
 * @throws [RuntimeException] when an anonymous party cannot not be resolved
 */
fun ServiceHub.wellKnownParty(abstractParty: AbstractParty): Party =
        abstractParty as? Party
                ?: identityService.wellKnownPartyFromAnonymous(abstractParty)
                ?: throw RuntimeException("Anonymous party could not be resolved: ${abstractParty.nameOrNull()?:"unknown"}")

/** Resolve participating parties */
@Suppress("unused")
fun ServiceHub.wellKnownParticipants(state: ContractState): Set<Party> =
        state.participants.map { wellKnownParty(it) }.toSet()

/** Resolve participating parties */
@Suppress("unused")
fun ServiceHub.wellKnownParticipants(states: Iterable<ContractState>): Set<Party> =
        states.map { it.participants }.flatten().map { wellKnownParty(it) }.toSet()

/** Resolve participating parties */
@Suppress("unused")
fun ServiceHub.wellKnownCounterParties(state: ContractState): Set<Party> =
        wellKnownParticipants(state).filter { !myInfo.legalIdentities.contains(it) }.toSet()


/** Resolve participating parties */
@Suppress("unused")
fun ServiceHub.wellKnownCounterParties(states: Iterable<ContractState>): Set<Party> =
        wellKnownParticipants(states).filter { !myInfo.legalIdentities.contains(it) }.toSet()


