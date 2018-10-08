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

import com.github.manosbatsis.corbeans.corda.test.MockNetworkFlowTest
import com.github.manosbatsis.corbeans.cordapp.flow.proposal.simple.propose.ProposeFlow
import net.corda.core.toFuture
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class AccordanceWorkflowTest: MockNetworkFlowTest() {

    override val cordappPackages: List<String> = listOf("com.github.manosbatsis.corbeans")

    @Test
    fun `Run workflow to completion`() {
        val accordanceInputId = UUID.randomUUID().toString()

        // Setup a vault subscriber to wait for successful upload of the proposal to NodeB
        val nodeBVaultUpdate = bobNode.services.vaultService.updates.toFuture()


        // Proposal submission by Alice
        // =============================
        val flow1 = aliceNode.startFlow(
                ProposeFlow(
                        ProposalInput(
                                accordanceInputId,
                                bob,
                                "test"
                        )
                )
        )
        // Wait for the flow to finish
        val proposalRef = flow1.getOrThrow().tx.outRef<ProposalState>(0)
        val proposalLinearId = proposalRef.state.data.linearId
        // Wait for NodeB to include it's copy in the vault
        nodeBVaultUpdate.get()


        // Fetch the latest copy of the state from both nodes
        val latestFromA = aliceNode.transaction {
            aliceNode.services.latest<ProposalState>(proposalLinearId)
        }
        val latestFromB = bobNode.transaction {
            bobNode.services.latest<ProposalState>(proposalLinearId)
        }

        // Confirm the state is as expected
        assertEquals(ProposalStatus.NEW, proposalRef.state.data.status)
        assertEquals(accordanceInputId, proposalRef.state.data.processId)
        assertEquals(alice, proposalRef.state.data.initiatingParty)
        assertEquals(bob, proposalRef.state.data.counterParty)
        assertEquals(proposalRef, latestFromA)
        assertEquals(proposalRef, latestFromB)


        // Setup a vault subscriber to pause until the final update is in NodeA and NodeB
        val nodeAVaultUpdate = aliceNode.services.vaultService.updates.toFuture()
        val secondNodeBVaultUpdate = bobNode.services.vaultService.updates.toFuture()
        // Run the manual completion flow from NodeB


        // Proposal approved by Bob
        // ==========================
        val flow2 = bobNode.startFlow(
                CompleteFlow(latestFromB.ref, ProposalStatus.APPROVED))
        // wait for the flow to end
        val completedRef = flow2.getOrThrow()
        // wait for the vault updates to stabilise
        nodeAVaultUpdate.get()
        secondNodeBVaultUpdate.get()

        // Fetch the latest copies from the vault
        val finalFromA = aliceNode.transaction {
            aliceNode.services.latest<ProposalState>(proposalLinearId)
        }
        val finalFromB = bobNode.transaction {
            bobNode.services.latest<ProposalState>(proposalLinearId)
        }

        // Confirm the state is as expected
        assertEquals(ProposalStatus.APPROVED, completedRef.state.data.status)
        assertEquals(accordanceInputId, completedRef.state.data.processId)
        assertEquals(alice, completedRef.state.data.initiatingParty)
        assertEquals(bob, completedRef.state.data.counterParty)
        assertEquals(completedRef, finalFromA)
        assertEquals(completedRef, finalFromB)
    }
}

