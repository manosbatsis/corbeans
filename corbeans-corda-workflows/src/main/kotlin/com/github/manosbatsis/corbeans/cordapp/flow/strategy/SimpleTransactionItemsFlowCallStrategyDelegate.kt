package com.github.manosbatsis.corbeans.cordapp.flow.strategy

import co.paralleluniverse.fibers.Suspendable
import com.github.manosbatsis.corbeans.cordapp.helpers.ProgressTrackerUtil
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.*
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

class SimpleFlowCallStrategyDelegate: FlowCallStrategyDelegate {

    /** Provide a delegate for the given client flow */
    @Suspendable
    override fun transact(clientFlow: StrategyBackedFlowLogic): SignedTransaction {

        // Build the transaction
        clientFlow.progressTracker.currentStep = ProgressTrackerUtil.Companion.BUILDING
        val transactionBuilder = createTransactionBuilder(clientFlow)

        // Sign it
        clientFlow.progressTracker.currentStep = ProgressTrackerUtil.Companion.SIGNING
        val initialTransaction = clientFlow.serviceHub.signInitialTransaction(
                transactionBuilder,
                clientFlow.ourIdentity.owningKey)

        // Send any keys and certificates so the signers can verify each other's identityService.
        // We call `toSet` in case there are multiple instances of the same party
        val sessions = clientFlow.toFlowSessions(*transactionBuilder.outputStates().toTypedArray())
        clientFlow.subFlow(IdentitySyncFlow.Send(
                sessions, initialTransaction.tx, ProgressTrackerUtil.Companion.SYNCING.childProgressTracker()))

        // Collect signatures
       clientFlow.progressTracker.currentStep = ProgressTrackerUtil.Companion.COLLECTING
        val stx = clientFlow.subFlow(CollectSignaturesFlow(
                initialTransaction,
                sessions,
                listOf(clientFlow.ourIdentity.owningKey),
                ProgressTrackerUtil.Companion.COLLECTING.childProgressTracker())
        )

        // Finalise the transaction.
        clientFlow.progressTracker.currentStep = ProgressTrackerUtil.Companion.FINALISING
        return clientFlow.subFlow(FinalityFlow(stx, ProgressTrackerUtil.Companion.FINALISING.childProgressTracker()))
    }

    @Suspendable
    fun createTransactionBuilder(clientFlow: StrategyBackedFlowLogic): TransactionBuilder {

        val items = clientFlow.getTransactionItems()
        val txb = TransactionBuilder(clientFlow.getNotary())
        items.inputStates.forEach { txb.addInputState(it) }
        items.outputStates.forEach { addOutputState(it, clientFlow, txb) }
        items.outputStateAndContracts.forEach { addOutputState(it, clientFlow, txb) }
        items.outputTransactionStates.forEach { addOutputState(it, clientFlow, txb) }
        items.attachments.forEach { txb.addAttachment(it) }
        addTimeWindow(clientFlow, txb)
        return txb
    }

    /*
     // Step 1. Initialisation.

            // Retreive existing guarantee by linearId...
            progressTracker.currentStep = GuaranteeUpdateBaseFlow.Companion.INITIALISING
            val guaranteeToUpdate = getGuaranteeByLinearId(linearId)

            // ... and create a clone with the new attachment
            val newGuarantee = this.buildNewState(guaranteeToUpdate)

            // Establish signers
            val signers = newGuarantee.participants
            val signerKeys = signers.map { it.owningKey }

            // Step 3. Build the transaction
            progressTracker.currentStep = GuaranteeUpdateBaseFlow.Companion.BUILDING
            val utx = this.buildTransactionBuilder(guaranteeToUpdate, newGuarantee, signerKeys)

            // Step 4. Sign the transaction.
            progressTracker.currentStep = GuaranteeUpdateBaseFlow.Companion.SIGNING
            val ptx = serviceHub.signInitialTransaction(utx, this.ourIdentity.owningKey)

            // Stage 5. Send any keys and certificates so the signers can verify each other's identityService.
            // We call `toSet` in case there are multiple instances of the same party
            val sessions = this.getSessions(newGuarantee);
            logger.info("signer sessions: ", sessions)
            subFlow(IdentitySyncFlow.Send(sessions, ptx.tx, GuaranteeUpdateBaseFlow.Companion.SYNCING.childProgressTracker()))

            // Step 6. Collect signatures
            progressTracker.currentStep = GuaranteeUpdateBaseFlow.Companion.COLLECTING
            val stx = subFlow(CollectSignaturesFlow(
                    ptx,
                    sessions,
                    listOf(this.ourIdentity.owningKey),
                    GuaranteeUpdateBaseFlow.Companion.COLLECTING.childProgressTracker())
            )

            // Step 7. Finalise the transaction.
            progressTracker.currentStep = GuaranteeUpdateBaseFlow.Companion.FINALISING
            return subFlow(FinalityFlow(stx, GuaranteeUpdateBaseFlow.Companion.FINALISING.childProgressTracker()))
     */

    @Suspendable
    fun addOutputState(state: ContractState, flow: StrategyBackedFlowLogic, txb: TransactionBuilder) {
        val contractClassName: ContractClassName = flow.getContractClassName(state)
        addOutputState(StateAndContract(state, contractClassName), flow, txb)

    }

    @Suspendable
    fun addOutputState(stateAndContract: StateAndContract, flow: StrategyBackedFlowLogic, txb: TransactionBuilder) {
        val notary: Party = flow.getNotary(stateAndContract)
        addOutputState(TransactionState(stateAndContract.state, stateAndContract.contract, notary), flow, txb )
    }

    @Suspendable
    fun addOutputState(transactionState: TransactionState<*>, flow: StrategyBackedFlowLogic, txb: TransactionBuilder) {
        val contractClassName: ContractClassName = transactionState.contract
        val signers = transactionState.data.participants
        val signerKeys = signers.map { it.owningKey }
        val command = Command(
                flow.getCommandData(transactionState),
                signerKeys)
        txb.addOutputState(transactionState)
        txb.addCommand(command)
    }

    @Suspendable
    fun addTimeWindow(flow: StrategyBackedFlowLogic, txb: TransactionBuilder) {
        // NO-OP
    }
}