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
package com.github.manosbatsis.corbeans.cordapp.flow.accordance.simple

import com.github.manosbatsis.corbeans.corda.test.MockNetworkFlowTest
import net.corda.core.toFuture
import net.corda.core.utilities.getOrThrow
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class AccordanceWorkflowTest: MockNetworkFlowTest() {

    override val cordappPackages: List<String> = listOf("com.github.manosbatsis.corbeans")

    @Test
    fun `Run workflow to completion`() {
        // Setup a vault subscriber to wait for successful upload of the proposal to NodeB
        val nodeBVaultUpdate = bobNode.services.vaultService.updates.toFuture()
        // Kick of the proposal flow
        val flow1 = aliceNode.startFlow(
                SubmitAccordanceApprovalFlow(
                        AccordanceInput(
                                UUID.randomUUID().toString(),
                                bob,
                                "test"
                        )
                )
        )
        // Wait for the flow to finish
        val proposalRef = flow1.getOrThrow()
        val proposalLinearId = proposalRef.state.data.linearId
        // Wait for NodeB to include it's copy in the vault
        nodeBVaultUpdate.get()
        // Fetch the latest copy of the state from both nodes
        val latestFromA = aliceNode.transaction {
            aliceNode.services.latest<AccordanceState>(proposalLinearId)
        }
        val latestFromB = bobNode.transaction {
            bobNode.services.latest<AccordanceState>(proposalLinearId)
        }
        // Confirm the state is as expected
        assertEquals(AccordanceStatus.NEW, proposalRef.state.data.status)
        assertEquals("1234", proposalRef.state.data.processId)
        assertEquals(alice, proposalRef.state.data.initiatingParty)
        assertEquals(bob, proposalRef.state.data.counterParty)
        assertEquals(proposalRef, latestFromA)
        assertEquals(proposalRef, latestFromB)
        // Setup a vault subscriber to pause until the final update is in NodeA and NodeB
        val nodeAVaultUpdate = aliceNode.services.vaultService.updates.toFuture()
        val secondNodeBVaultUpdate = bobNode.services.vaultService.updates.toFuture()
        // Run the manual completion flow from NodeB
        val flow2 = bobNode.startFlow(SubmitCompletionFlow(latestFromB.ref, AccordanceStatus.APPROVED))
        // wait for the flow to end
        val completedRef = flow2.getOrThrow()
        // wait for the vault updates to stabilise
        nodeAVaultUpdate.get()
        secondNodeBVaultUpdate.get()
        // Fetch the latest copies from the vault
        val finalFromA = aliceNode.transaction {
            aliceNode.services.latest<AccordanceState>(proposalLinearId)
        }
        val finalFromB = bobNode.transaction {
            bobNode.services.latest<AccordanceState>(proposalLinearId)
        }
        // Confirm the state is as expected
        assertEquals(AccordanceStatus.APPROVED, completedRef.state.data.status)
        assertEquals("1234", completedRef.state.data.processId)
        assertEquals(alice, completedRef.state.data.initiatingParty)
        assertEquals(bob, completedRef.state.data.counterParty)
        assertEquals(completedRef, finalFromA)
        assertEquals(completedRef, finalFromB)
    }
}

