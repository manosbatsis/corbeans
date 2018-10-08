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

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.StateRef
import net.corda.core.crypto.TransactionSignature
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.InitiatingFlow
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.seconds
import net.corda.core.utilities.unwrap

/**
 * Simple flow to complete a proposal submitted by another party and ensure both nodes
 * end up with a fully signed copy of the status either as APPROVED, or REJECTED
 */
@InitiatingFlow
class CompleteFlow(private val ref: StateRef, private val verdict: ProposalStatus) : FlowLogic<StateAndRef<ProposalState>>() {
    init {
        require(verdict in setOf(ProposalStatus.APPROVED, ProposalStatus.REJECTED)) {
            "Verdict must be a final status"
        }
    }

    @Suspendable
    override fun call(): StateAndRef<ProposalState> {
        // DOCSTART 1
        val criteria = QueryCriteria.VaultQueryCriteria(stateRefs = listOf(ref))
        val latestRecord = serviceHub.vaultService.queryBy<ProposalState>(criteria).states.single()
        // DOCEND 1

        // Check the protocol hasn't already been run
        require(latestRecord.ref == ref) {
            "Input dataAccordance $ref is not latest version $latestRecord"
        }
        // Require that the status is still modifiable
        require(latestRecord.state.data.status == ProposalStatus.NEW) {
            "Input dataAccordance not modifiable ${latestRecord.state.data.status}"
        }
        // Check we are the correct Party to run the protocol. Note they will counter check this too.
        require(serviceHub.myInfo.isLegalIdentity(latestRecord.state.data.counterParty)) {
            "The counterParty must give the verdict"
        }

        // DOCSTART 2
        // Modify the status field for new output. We use copy, to ensure no other modifications.
        // It is especially important for a LinearState that the linearId is copied across,
        // not accidentally assigned a new random id.
        val newState = latestRecord.state.data.copy(status = verdict)

        // We have to use the original notary for the new transaction
        val notary = latestRecord.state.notary

        // Get and populate the new TransactionBuilder
        // To destroy the old proposal status and replace with the new completion status.
        // Also add the Completed command with keys of all parties to signal the Tx purpose
        // to the Contract verify method.
        val tx = TransactionBuilder(notary).
                withItems(
                        latestRecord,
                        StateAndContract(newState, ProposalContract::class.java.canonicalName),
                        Command(ProposalContract.Commands.Completed(),
                                listOf(ourIdentity.owningKey, latestRecord.state.data.initiatingParty.owningKey)))
        tx.setTimeWindow(serviceHub.clock.instant(), 60.seconds)
        // We can sign this transaction immediately as we have already checked all the fields and the decision
        // is ultimately a manual one from the caller.
        // As a SignedTransaction we can pass the data around certain that it cannot be modified,
        // although we do require further signatures to complete the process.
        val selfSignedTx = serviceHub.signInitialTransaction(tx)
        //DOCEND 2
        // Send the signed transaction to the originator and await their signature to confirm
        val session = initiateFlow(newState.initiatingParty)
        val allPartySignedTx = session.sendAndReceive<TransactionSignature>(selfSignedTx).unwrap {
            // Add their signature to our unmodified transaction. To check they signed the same tx.
            val agreedTx = selfSignedTx + it
            // Receive back their signature and confirm that it is for an unmodified transaction
            // Also that the only missing signature is from teh Notary
            agreedTx.verifySignaturesExcept(notary.owningKey)
            // Recheck the data of the transaction. Note we run toLedgerTransaction on the WireTransaction
            // as we do not have all the signature.
            agreedTx.tx.toLedgerTransaction(serviceHub).verify()
            // return the SignedTransaction to notarise
            agreedTx
        }
        // DOCSTART 4
        // Notarise and distribute the completed transaction.
        subFlow(FinalityFlow(allPartySignedTx, setOf(newState.initiatingParty)))
        // DOCEND 4
        // Return back the details of the completed status/transaction.
        return allPartySignedTx.tx.outRef(0)
    }
}