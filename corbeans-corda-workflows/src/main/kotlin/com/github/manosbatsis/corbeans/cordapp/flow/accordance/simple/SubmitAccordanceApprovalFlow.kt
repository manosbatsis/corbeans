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
package com.github.manosbatsis.corbeans.cordapp.flow.accordance.simple

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.corbeans.cordapp.flow.accordance.simple.AccordanceContract.Companion.CONTRACT_ID
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.seconds

/**
 * Simple flow to create a workflow status, sign and notarise it.
 * The protocol then sends a copy to the other node. We don't require the other party to sign
 * as their approval/rejection is to follow.
 */
class SubmitAccordanceApprovalFlow(
        private val accordanceInput: AccordanceInput
) : FlowLogic<StateAndRef<AccordanceState>>() {

    @Suspendable
    override fun call(): StateAndRef<AccordanceState> {
        // Manufacture an initial status
        val dataAccordanceProposal = AccordanceState(
                processId = accordanceInput.processId,
                initiatingParty = this.ourIdentity,
                counterParty = accordanceInput.counterParty,
                context = accordanceInput.context)
        // identify a notary. This might also be done external to the flow
        val notary = serviceHub.networkMapCache.notaryIdentities.first()
        // Create the TransactionBuilder and populate with the new status.
        val tx = TransactionBuilder(notary).withItems(
                StateAndContract(dataAccordanceProposal, CONTRACT_ID),
                Command(AccordanceContract.Commands.Issue(), listOf(dataAccordanceProposal.initiatingParty.owningKey)))
        tx.setTimeWindow(serviceHub.clock.instant(), 60.seconds)
        // We can automatically sign as there is no untrusted data.
        val signedTx = serviceHub.signInitialTransaction(tx)
        // Notarise and distribute.
        subFlow(FinalityFlow(signedTx, setOf(accordanceInput.counterParty)))
        // Return the initial status
        return signedTx.tx.outRef(0)
    }
}