package com.github.manosbatsis.corbeans.cordapp.flow.linear

import co.paralleluniverse.fibers.Suspendable
import net.corda.confidential.IdentitySyncFlow
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.seconds
import java.security.PublicKey

/**
 * Base class for guarantee updating flows.
 */
abstract class GuaranteeUpdateBaseFlow(private val linearId: UniqueIdentifier) : AbstractLinearStateFlow() {




    /**
     * Get the new state.
     */
    abstract fun buildNewState(guaranteeToUpdate: StateAndRef<GuaranteeState>): GuaranteeState

    /**
     * Build a transaction builder. This fallback implementation can be easily chained
     * and includes the input and output states, the implementation specific command
     * and a time window of 30 seconds.
     */
    open fun buildTransactionBuilder(guaranteeToUpdate: StateAndRef<GuaranteeState>,
                                     newGuarantee: GuaranteeState,
                                     signerKeys: List<PublicKey>): TransactionBuilder {
        return TransactionBuilder(firstNotary)
                .addInputState(guaranteeToUpdate)
                .addOutputState(newGuarantee, GuaranteeContract.GUARANTEE_CONTRACT_ID)
                .addCommand(this.getCommand(), signerKeys)
                .setTimeWindow(serviceHub.clock.instant(), 30.seconds)
    }


    @Suspendable
    override fun call(): SignedTransaction {
        // Step 1. Initialisation.

        // Retreive existing guarantee by linearId...
        progressTracker.currentStep = INITIALISING
        val guaranteeToUpdate = getGuaranteeByLinearId(linearId)

        // ... and create a clone with the new attachment
        val newGuarantee = this.buildNewState(guaranteeToUpdate)

        // Establish signers
        val signers = newGuarantee.participants
        val signerKeys = signers.map { it.owningKey }

        // Step 3. Build the transaction
        progressTracker.currentStep = BUILDING
        val utx = this.buildTransactionBuilder(guaranteeToUpdate, newGuarantee, signerKeys)

        // Step 4. Sign the transaction.
        progressTracker.currentStep = SIGNING
        val ptx = serviceHub.signInitialTransaction(utx, this.ourIdentity.owningKey)

        // Stage 5. Send any keys and certificates so the signers can verify each other's identityService.
        // We call `toSet` in case there are multiple instances of the same party
        val sessions = this.getSessions(newGuarantee);
        logger.info("signer sessions: ", sessions)
        subFlow(IdentitySyncFlow.Send(sessions, ptx.tx, SYNCING.childProgressTracker()))

        // Step 6. Collect signatures
        progressTracker.currentStep = COLLECTING
        val stx = subFlow(CollectSignaturesFlow(
                ptx,
                sessions,
                listOf(this.ourIdentity.owningKey),
                COLLECTING.childProgressTracker())
        )

        // Step 7. Finalise the transaction.
        progressTracker.currentStep = FINALISING
        return subFlow(FinalityFlow(stx, FINALISING.childProgressTracker()))
    }
}
