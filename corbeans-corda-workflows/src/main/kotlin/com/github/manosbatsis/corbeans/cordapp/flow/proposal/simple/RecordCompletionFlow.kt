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
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.FlowSession
import net.corda.core.flows.InitiatedBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.unwrap

/**
 * Simple flow to receive the final decision on a proposal.
 * Then after checking to sign it and eventually store the fully notarised
 * transaction to the ledger.
 */
@InitiatedBy(CompleteFlow::class)
class RecordCompletionFlow(private val sourceSession: FlowSession) : FlowLogic<Unit>() {
    @Suspendable
    override fun call() {
        // DOCSTART 3
        // First we receive the verdict transaction signed by their single key
        val completeTx = sourceSession.receive<SignedTransaction>().unwrap {
            // Check the transaction is signed apart from our own key and the notary
            it.verifySignaturesExcept(ourIdentity.owningKey, it.tx.notary!!.owningKey)
            // Check the transaction data is correctly formed
            val ltx = it.toLedgerTransaction(serviceHub, false)
            ltx.verify()
            // Confirm that this is the expected type of transaction
            require(ltx.commands.single().value is ProposalContract.Commands.Completed) {
                "Transaction must represent a workflow completion"
            }
            // Check the context dependent parts of the transaction as the
            // Contract verify method must not use serviceHub queries.
            val state = ltx.outRef<ProposalState>(0)
            require(serviceHub.myInfo.isLegalIdentity(state.state.data.initiatingParty)) {
                "Proposal not one of our original proposals"
            }
            require(state.state.data.counterParty == sourceSession.counterparty) {
                "Proposal not for sent from correct initiatingParty"
            }
            it
        }
        // DOCEND 3
        // Having verified the SignedTransaction passed to us we can sign it too
        val ourSignature = serviceHub.createSignature(completeTx)
        // Send our signature to the other party.
        sourceSession.send(ourSignature)
        // N.B. The FinalityProtocol will be responsible for Notarising the SignedTransaction
        // and broadcasting the result to us.
    }
}