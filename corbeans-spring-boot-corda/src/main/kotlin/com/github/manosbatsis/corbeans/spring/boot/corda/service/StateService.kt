/*
 *     Corbeans: Corda integration for Spring Boot
 *     Copyright (C) 2018 Manos Batsis
 *
 *     This library is free software; you can redistribute it and/or
 *     modify it under the terms of the GNU Lesser General Public
 *     License as published by the Free Software Foundation; either
 *     version 3 of the License, or (at your option) any later version.
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
package com.github.manosbatsis.corbeans.spring.boot.corda.service

import net.corda.core.contracts.ContractState
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.DataFeed
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.*

/**
 * Short-lived helper, used for vault operations
 * on a specific `ContractState` type
 * @param T the state type
 */
class StateService<T : ContractState>(
        private val contractStateType: Class<T>,
        private val nodeService: CordaNodeService
    ) {

    companion object {
        private val DEFAULT_CRITERIA = QueryCriteria.VaultQueryCriteria()
        private val DEFAULT_PAGESIZE = 10
        private val DEFAULT_PAGING = PageSpecification(pageSize = DEFAULT_PAGE_SIZE, pageNumber = -1)
        private val DEFAULT_SORT = Sort(emptySet())
    }


    fun findByLinearId(linearId: UniqueIdentifier): StateAndRef<T>? {
        val criteria = QueryCriteria.LinearStateQueryCriteria(
                linearId = listOf(linearId),
                participants = listOf(nodeService.myIdentity))
        return this.queryBy(criteria, DEFAULT_PAGING, DEFAULT_SORT).states.firstOrNull()
    }

    fun findByLinearId(linearId: String): StateAndRef<T>? {
        return findByLinearId(UniqueIdentifier.fromString(linearId))
    }

    /**
     * Query the vault for states matching the given criteria,
     * applying the given page number, size and sorting specifications if any
     * @see CordaRPCOps.vaultQueryBy
     */
    fun queryBy(
            criteria: QueryCriteria = DEFAULT_CRITERIA,
            pageNumber: Int = 1,
            pageSize: Int = DEFAULT_PAGESIZE,
            sort: Sort = DEFAULT_SORT
    ): Vault.Page<T> {
        return queryBy(criteria, PageSpecification(pageNumber, pageSize), sort)
    }

    /**
     * Query the vault for states matching the given criteria,
     * applying the given paging and sorting specifications if any
     * @see CordaRPCOps.vaultQueryBy
     */
    fun queryBy(
            criteria: QueryCriteria = DEFAULT_CRITERIA,
            paging: PageSpecification = DEFAULT_PAGING,
            sort: Sort = DEFAULT_SORT
    ): Vault.Page<T> {

        return nodeService.proxy().vaultQueryBy(criteria, paging, sort, contractStateType)
    }

    /**
     * Query the vault for states of type `T`
     * @see CordaRPCOps.vaultQuery
     */
    fun query(): Vault.Page<T> {
        return nodeService.proxy().vaultQuery(contractStateType)
    }

    /**
     * Track the vault for events of `T` states matching the given criteria,
     * applying the given page number, size and sorting specifications if any
     * @see CordaRPCOps.vaultTrackBy
     */
    fun trackBy(
            criteria: QueryCriteria = DEFAULT_CRITERIA,
            pageNumber: Int = 1,
            pageSize: Int = DEFAULT_PAGESIZE,
            sort: Sort = DEFAULT_SORT
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> {
        return this.trackBy(criteria, PageSpecification(pageNumber, pageSize), sort)
    }

    /**
     * Track the vault for events of `T` states matching the given criteria,
     * applying the given paging and sorting specifications if any
     * @see CordaRPCOps.vaultTrackBy
     */
    fun trackBy(
            criteria: QueryCriteria = DEFAULT_CRITERIA,
            paging: PageSpecification = DEFAULT_PAGING,
            sort: Sort = DEFAULT_SORT
    ): DataFeed<Vault.Page<T>, Vault.Update<T>> {
        return nodeService.proxy().vaultTrackBy(criteria, paging, sort, contractStateType)
    }

    /**
     * Track the vault for events of `T` states
     * @see CordaRPCOps.track
     */
    fun track(): DataFeed<Vault.Page<T>, Vault.Update<T>> {
        return nodeService.proxy().vaultTrack(contractStateType)
    }

}
