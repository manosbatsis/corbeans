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
package com.github.manosbatsis.corbeans.test

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.testing.core.ALICE_NAME
import net.corda.testing.core.BOB_NAME
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.StartedMockNode
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach

abstract class MockNetworkFlowTest {

    protected lateinit var mockNet: MockNetwork
    protected lateinit var aliceNode: StartedMockNode
    protected lateinit var bobNode: StartedMockNode
    protected lateinit var alice: Party
    protected lateinit var bob: Party

    abstract val cordappPackages: List<String>

    // Helper method to locate the latest Vault version of a LinearState
    protected inline fun <reified T : LinearState> ServiceHub.latest(ref: UniqueIdentifier): StateAndRef<T> {
        val linearHeads = vaultService.queryBy<T>(QueryCriteria.LinearStateQueryCriteria(uuid = listOf(ref.id)))
        return linearHeads.states.single()
    }

    @BeforeEach
    fun setup() {
        mockNet = MockNetwork(threadPerNode = true, cordappPackages = cordappPackages)
        aliceNode = mockNet.createPartyNode(ALICE_NAME)
        bobNode = mockNet.createPartyNode(BOB_NAME)
        alice = aliceNode.services.myInfo.identityFromX500Name(ALICE_NAME)
        bob = bobNode.services.myInfo.identityFromX500Name(BOB_NAME)
    }

    @AfterEach
    fun cleanUp() {
        mockNet.stopNodes()
    }



}

