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

import com.github.manosbatsis.corbeans.cordapp.flow.delegating.tx.TxStrategyDelegateFlow.Companion.SYNCING
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.FlowSession
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder


/**
 * Basic TX delegate strategy flow used to sync identities, counter-sign, notarize and distribute a TX
 */
open class CounterSignFlowCallStrategyDelegateFlow(
        config: TxStrategyDelegateConfig
) : TxStrategyDelegateFlow(
        config
) {

    /** Send any keys and certificates so the signers can verify each other's identityService. */
    override fun createFlowSessions(transactionBuilder: TransactionBuilder,
                                    initialTx: SignedTransaction): Set<FlowSession>? {
        // get flow sessions
        val sessions = toFlowSessions(
                *transactionBuilder.outputStates().toTypedArray())
        // sync identities
        subFlow(IdentitySyncFlow.Send(
                sessions,
                initialTx.tx, SYNCING.childProgressTracker()))
        return sessions
    }

}