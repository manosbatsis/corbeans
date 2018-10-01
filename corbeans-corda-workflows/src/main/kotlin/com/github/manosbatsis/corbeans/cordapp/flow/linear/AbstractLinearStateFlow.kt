package com.github.manosbatsis.corbeans.cordapp.flow.linear

import com.github.manosbatsis.corbeans.cordapp.flow.strategy.StrategyBackedFlowLogic
import net.corda.core.contracts.LinearState
import net.corda.core.flows.FlowLogic

/**
 * Abstract base class for generic [LinearState]-focused [FlowLogic] implementations
 */
abstract class AbstractLinearStateFlow<T: LinearState>(stateType: Class<T>) : StrategyBackedFlowLogic() {
/*

    @Suspendable
    override fun call(): SignedTransaction {
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
    }
*/
}