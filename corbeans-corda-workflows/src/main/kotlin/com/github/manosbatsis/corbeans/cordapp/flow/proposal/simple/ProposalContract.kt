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

import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

/**
 * Minimal contract to encode a minimal proposal workflow
 */
data class ProposalContract(val blank: Unit? = null) : Contract {



    interface Commands : CommandData {
        class Create : TypeOnlyCommandData(), Commands  // Record receipt of deal details
        class Resolve : TypeOnlyCommandData(), Commands  // Record match
    }

    /**
     * The verify method locks down the allowed transactions to contain just a single proposal being
     * created/modified and the only modification allowed is to the status field.
     */
    override fun verify(tx: LedgerTransaction) {
        val command = tx.commands.requireSingleCommand<Commands>()
        requireNotNull(tx.timeWindow) { "must have a time-window" }
        when (command.value) {
            is Commands.Create -> {
                requireThat {
                    "A new proposal must not include any inputs" using (tx.inputs.isEmpty())
                    "A new proposal must be in a unique transaction" using (tx.outputs.size == 1)
                }
                val issued = tx.outputsOfType<ProposalState>().single()
                requireThat {
                    "A new proposal requires the initiatingParty Party as signer" using (command.signers.contains(issued.initiatingParty.owningKey))
                    "A new proposal status must be NEW" using (issued.status == ProposalStatus.NEW)
                }
            }
            is Commands.Resolve -> {
                val stateGroups = tx.groupStates(ProposalState::class.java) { it.linearId }
                require(stateGroups.size == 1) { "Must be only a single proposal in transaction" }
                for ((inputs, outputs) in stateGroups) {
                    val before = inputs.single()
                    val after = outputs.single()
                    requireThat {
                        "Only a non-final proposal can be modified" using (before.status == ProposalStatus.NEW)
                        "Output must be a final status" using (after.status in setOf(ProposalStatus.APPROVED, ProposalStatus.REJECTED))
                        "Resolve command can only change status" using (before == after.copy(status = before.status))
                        "Resolve command requires the initiatingParty Party as signer" using (command.signers.contains(before.initiatingParty.owningKey))
                        "Resolve command requires the counterParty as signer" using (command.signers.contains(before.counterParty.owningKey))
                    }
                }
            }
            else -> throw IllegalArgumentException("Unrecognised Command $command")
        }
    }
}