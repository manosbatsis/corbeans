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
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.CounterSignFlowCallStrategyDelegateFlow
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateFlow
import com.github.manosbatsis.corbeans.cordapp.flow.proposal.simple.input.ResolveProposalInput
import com.github.manosbatsis.corbeans.cordapp.model.TransactionItems
import net.corda.core.contracts.CommandData
import net.corda.core.flows.InitiatingFlow
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria

/**
 * Simple flow to complete a proposal submitted by another party and ensure both nodes
 * end up with a fully signed copy of the status either as APPROVED, or REJECTED
 */
@InitiatingFlow
class ResolveProposalFlow(
        val input: ResolveProposalInput
) : TxDelegatingFlow<CounterSignFlowCallStrategyDelegateFlow>() {

    init {
        require(input.verdict in setOf(ProposalStatus.APPROVED, ProposalStatus.REJECTED)) {
            "Verdict must be a final status"
        }
    }

    override val txDelegateType =
            CounterSignFlowCallStrategyDelegateFlow::class.java

    /** The contract identifier appropriate for the given contract state */
    override val contractClassName =
            ProposalContract::class.java.canonicalName

    /** The [CommandData] appropriate for the given contract and state */
    override val commandData = ProposalContract.Commands.Resolve()

    /** The transaction items used by the [TxStrategyDelegateFlow] */
    override fun getTransactionItems(): TransactionItems {

        val criteria = QueryCriteria.VaultQueryCriteria(stateRefs = listOf(input.ref))
        val latestRecord = serviceHub.vaultService.queryBy<ProposalState>(criteria).states.single()

        // Check the protocol hasn't already been run
        require(latestRecord.ref == input.ref) {
            "Input dataAccordance ${input.ref} is not latest version $latestRecord"
        }
        // Require that the status is still modifiable
        require(latestRecord.state.data.status == ProposalStatus.NEW) {
            "Input dataAccordance not modifiable ${latestRecord.state.data.status}"
        }
        // Check we are the correct Party to run the protocol. Note they will counter check this too.
        require(serviceHub.myInfo.isLegalIdentity(latestRecord.state.data.counterParty)) {
            "The counterParty must give the verdict"
        }

        return TransactionItems(
                inputStates = listOf(latestRecord),
                outputStates = listOf(
                        latestRecord.state.data.copy(
                                status = input.verdict
                        )
                )
        )
    }
}