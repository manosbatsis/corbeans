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
package com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.corbeans.cordapp.extention.wellKnownCounterParties
import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateFlow.Companion.FINALISING
import net.corda.core.contracts.TransactionState
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowSession
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.security.PublicKey


/**
 * Basic TX delegate strategy flow used to notarize and distribute a TX when there is no need to countersign
 */
open class NoCounterSignDistributionFlowCallStrategyDelegateFlow(
        config: TxStrategyDelegateConfig
) : TxStrategyDelegateFlow(
        config
) {

    /** Skip session and counter-signatures as there is no untrusted data */
    @Suspendable
    override fun createFlowSessions(transactionBuilder: TransactionBuilder,
                                    initialTx: SignedTransaction): Set<FlowSession>? = null


    /** Notarize and distribute a finalised TX without requiring signature from counter-parties */
    @Suspendable
    override fun createFinalizedTransaction(notarizableTx: SignedTransaction): SignedTransaction {
        val finalizedTx = subFlow(FinalityFlow(
                notarizableTx,
                extraParticipants = serviceHub.wellKnownCounterParties(notarizableTx.tx.outputStates),
                FINALISING.childProgressTracker()))
        return finalizedTx
    }

    @Suspendable
    override fun getSigners(transactionState: TransactionState<*>): List<PublicKey> {
        return listOf(ourIdentity.owningKey)
    }

}