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