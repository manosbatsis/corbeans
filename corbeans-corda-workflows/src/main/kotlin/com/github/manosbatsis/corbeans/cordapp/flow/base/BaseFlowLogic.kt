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
package com.github.manosbatsis.corbeans.cordapp.flow.base

import com.github.manosbatsis.corbeans.cordapp.extention.getFirstNotary
import com.github.manosbatsis.corbeans.cordapp.extention.getNotaryByOrganisation
import com.github.manosbatsis.corbeans.cordapp.extention.wellKnownParticipants
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.TransactionState
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.identity.Party

/**
 * Base [FlowLogic] implrmrntation, includes common utilities
 */
abstract class BaseFlowLogic<out T> : FlowLogic<T>() {

    /** Get the first notary found in the network map */
    fun getFirstNotary(): Party = serviceHub.getFirstNotary()

    /** Get the first notary matching the given organisation name*/
    fun getNotaryByOrganisation(organisation: String) : Party =
            serviceHub.getNotaryByOrganisation(organisation)

    /** Get the sessions of counter-parties that need to sign a transaction  */
    fun toFlowSessions(vararg states: TransactionState<*>): Set<FlowSession> =
            toFlowSessions(*states.asIterable().mapNotNull { it.data }.toTypedArray())

    /** Get the sessions of counter-parties that need to sign a transaction  */
    fun toFlowSessions(vararg states: ContractState): Set<FlowSession> =
            states.toList().mapNotNull { serviceHub.wellKnownParticipants(it) }
                    .flatten().filter { it.name != this.ourIdentity.name }
                    .map { party: Party -> initiateFlow(party) }
                    .toSet()
}