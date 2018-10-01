package com.github.manosbatsis.corbeans.cordapp.helpers

import net.corda.confidential.IdentitySyncFlow
import net.corda.core.flows.CollectSignaturesFlow
import net.corda.core.flows.FinalityFlow
import net.corda.core.utilities.ProgressTracker

class ProgressTrackerUtil {
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

        fun createBasicTracker() = ProgressTracker(INITIALISING, BUILDING, SIGNING, SYNCING, COLLECTING, FINALISING)
    }
}