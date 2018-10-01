package com.github.manosbatsis.corbeans.cordapp.helpers

import net.corda.core.contracts.LinearState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.FlowException
import net.corda.core.node.services.Vault
import net.corda.core.node.services.VaultService
import net.corda.core.node.services.vault.QueryCriteria

class TypedLinearStateVaultHelper<T: LinearState>(val vaultService: VaultService, val stateType: Class<out T>) {

    /** Lazy reusable set of the single state type used in vault queries etc. */
    val contractStateTypes by lazy {setOf(stateType)}

    /**
     * Get the state matching the linear ID if it exists
     * @throws [FlowException] if no match is found
     */
    fun getByLinearId(linearId: UniqueIdentifier, status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED): StateAndRef<T> {
        return findByLinearId(linearId, status) ?: throw FlowException("Linear ID $linearId not found.")
    }

    /** Get the state matching the linear ID if it exists, null otherwise */
    fun findByLinearId(linearId: UniqueIdentifier, status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED): StateAndRef<T>? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(
                participants = null,
                linearId = listOf(linearId),
                status = status,
                contractStateTypes = contractStateTypes)
        return vaultService.queryBy(stateType, criteria).states.singleOrNull()
    }

    /**
     * Get the state matching the external ID if it exists
     * @throws [FlowException] if no match is found
     */
    fun getByExternalId(externalId: String, status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED): StateAndRef<T> {
        return findByExternalId(externalId, status) ?: throw FlowException("External ID $externalId not found.")
    }
    /** Get the state matching the external ID if it exists, null otherwise */
    fun findByExternalId(externalId: String, status: Vault.StateStatus = Vault.StateStatus.UNCONSUMED): StateAndRef<T>? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(
                externalId = listOf(externalId),
                status = status
        )
        return vaultService.queryBy(stateType, criteria).states.singleOrNull()
    }

}