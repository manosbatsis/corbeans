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
import com.github.manosbatsis.corbeans.cordapp.extention.getFirstNotary
import com.github.manosbatsis.corbeans.cordapp.flow.base.BaseFlowLogic
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.Command
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.TransactionState
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.flows.FlowSession
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import java.security.PublicKey


/**
 * Base transaction executor delegate strategy
 * used by implementations of [TxDelegatingFlow]
 */
abstract class TxStrategyDelegateFlow(
        val config: TxStrategyDelegateConfig
) : BaseFlowLogic<SignedTransaction>() {

    companion object {
        object INITIALISING : ProgressTracker.Step("Performing initial steps.")
        object BUILDING : ProgressTracker.Step("Building and verifying transaction.")
        object SIGNING : ProgressTracker.Step("Signing transaction.")
        object SYNCING : ProgressTracker.Step("Syncing identities.") {
            override fun childProgressTracker() = IdentitySyncFlow.Send.tracker()
        }

        object COLLECTING : ProgressTracker.Step("Collecting counter-party signatures.") {
            override fun childProgressTracker() = CollectSignaturesFlow.tracker()
        }

        object FINALISING : ProgressTracker.Step("Finalising transaction.") {
            override fun childProgressTracker() = FinalityFlow.tracker()
        }

        fun tracker() = ProgressTracker(INITIALISING, BUILDING, SIGNING, SYNCING, COLLECTING, FINALISING)
    }

    override val progressTracker: ProgressTracker = tracker()

    /** Provide a delegate for the given client flow */
    @Suspendable
    override fun call(): SignedTransaction {
        //TODO: add onBeforeXX and onAfterXX methods

        // Build the transaction
        progressTracker.currentStep = BUILDING
        val transactionBuilder = createTransactionBuilder()

        // Sign it
        progressTracker.currentStep = SIGNING
        val initialTx = createinitialTx(transactionBuilder)

        // Send any keys and certificates so the signers can verify each other's identityService.
        // We call `toSet` in case there are multiple instances of the same party
        // TODO: progressTracker.currentStep = SYNCING
        val sessions = createFlowSessions(transactionBuilder, initialTx)

        // Collect signatures if sessions are present, use the original transaction otherwise
        progressTracker.currentStep = COLLECTING
        val notarizableTx = createCounterSignedTransaction(initialTx, sessions)

        // Finalise the transaction.
        progressTracker.currentStep = FINALISING
        return createFinalizedTransaction(notarizableTx)
    }

    /** Notarize and distribute a finalised TX */
    @Suspendable
    open fun createFinalizedTransaction(notarizableTx: SignedTransaction): SignedTransaction {
        val finalizedTx = subFlow(FinalityFlow(
                notarizableTx,
                FINALISING.childProgressTracker()))
        return finalizedTx
    }

    /** Collect signatures and return as new TX if [sessions] are present, return the original [initialTx] otherwise */
    @Suspendable
    fun createCounterSignedTransaction(initialTx: SignedTransaction,
                                       sessions: Set<FlowSession>?): SignedTransaction =
            if (sessions != null && sessions.isNotEmpty()) subFlow(CollectSignaturesFlow(
                    initialTx,
                    sessions,
                    listOf(ourIdentity.owningKey),
                    COLLECTING.childProgressTracker()))
            else initialTx


    /** Send any keys and certificates so the signers can verify each other's identityService. */
    @Suspendable
    open fun createFlowSessions(transactionBuilder: TransactionBuilder,
                                initialTx: SignedTransaction): Set<FlowSession>? {
        val sessions = toFlowSessions(*transactionBuilder.outputStates().toTypedArray())
        subFlow(IdentitySyncFlow.Send(
                sessions,
                initialTx.tx, SYNCING.childProgressTracker()))
        return sessions
    }

    @Suspendable
    fun createinitialTx(transactionBuilder: TransactionBuilder): SignedTransaction {
        val initialTx = serviceHub.signInitialTransaction(
                transactionBuilder,
                ourIdentity.owningKey)
        return initialTx
    }


    /**
     * Create a fully-configured [TransactionBuilder]
     */
    @Suspendable
    fun createTransactionBuilder(): TransactionBuilder {

        val txb = TransactionBuilder(serviceHub.getFirstNotary())
        val items = config.transactionItems
        // add input states
        items.inputStates.forEach { txb.addInputState(it) }
        // add input states
        items.outputStates.forEach { addOutputState(it, txb) }
        items.outputStateAndContracts.forEach { addOutputState(it, txb) }
        items.outputTransactionStates.forEach { addOutputState(it, txb) }
        // add attachments
        items.attachments.forEach { txb.addAttachment(it) }
        // add time window
        txb.setTimeWindow(config.timeWindow)
        return txb

    }


    @Suspendable
    fun addOutputState(state: ContractState, txb: TransactionBuilder) {
        addOutputState(StateAndContract(state, config.contractClassName), txb)

    }

    @Suspendable
        fun addOutputState(stateAndContract: StateAndContract, txb: TransactionBuilder) {
        addOutputState(TransactionState(stateAndContract.state, stateAndContract.contract, config.notary), txb)
    }

    @Suspendable
    fun addOutputState(transactionState: TransactionState<*>, txb: TransactionBuilder) {
        val signerKeys = getSigners(transactionState)
        val command = Command(
                config.commandData,
                signerKeys)
        txb.addOutputState(transactionState)
        txb.addCommand(command)
    }

    /**
     * Get the signers appropriate for the given [TransactionState]
     */
    @Suspendable
    open fun getSigners(transactionState: TransactionState<*>): List<PublicKey> {
        val signers = transactionState.data.participants
        val signerKeys = signers.map { it.owningKey }
        return signerKeys
    }
}