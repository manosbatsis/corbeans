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
package com.github.manosbatsis.corbeans.cordapp.flow.proposal.simple

import com.github.manosbatsis.corbeans.cordapp.flow.delegating.TxDelegatingFlow
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.NoCounterSignDistributionFlowCallStrategyDelegateFlow
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateFlow
import com.github.manosbatsis.corbeans.cordapp.flow.proposal.simple.input.CreateProposalInput
import com.github.manosbatsis.corbeans.cordapp.model.TransactionItems
import net.corda.core.contracts.CommandData

/**
 * Simple flow to create a new proposal, sign and notarise it.
 * The protocol then sends a copy to the other node(S). We don't require the other party to sign
 * as their approval/rejection is to follow.
 */
class CreateProposalFlow(
        private val proposalInput: CreateProposalInput
) : TxDelegatingFlow<NoCounterSignDistributionFlowCallStrategyDelegateFlow>() {

    override val txDelegateType =
            NoCounterSignDistributionFlowCallStrategyDelegateFlow::class.java

    /** The contract identifier appropriate for the given contract state */
    override val contractClassName =
            ProposalContract::class.java.canonicalName

    /** The [CommandData] appropriate for the given contract and state */
    override val commandData = ProposalContract.Commands.Create()

    /** The transaction items used by the [TxStrategyDelegateFlow] */
    override fun getTransactionItems(): TransactionItems {
        return TransactionItems(outputStates = listOf(
                ProposalState(
                        processId = proposalInput.processId,
                        initiatingParty = this.ourIdentity,
                        counterParty = proposalInput.counterParty,
                        context = proposalInput.context)
        ))
    }

}