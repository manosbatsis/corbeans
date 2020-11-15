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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.util

import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaRpcService
import com.github.manosbatsis.vaultaire.annotation.VaultaireDtoStrategyKeys
import com.github.manosbatsis.vaultaire.annotation.VaultaireGenerateDtoForDependency
import com.github.manosbatsis.vaultaire.annotation.VaultaireGenerateForDependency
import net.corda.bn.schemas.MembershipStateSchemaV1.PersistentMembershipState
import net.corda.bn.states.MembershipState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.Party
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder

/** Generate Query DSL and services for [MembershipState] */
@VaultaireGenerateForDependency(name = "accountInfoConditions",
        persistentStateType = PersistentMembershipState::class,
        contractStateType = MembershipState::class)
@VaultaireGenerateDtoForDependency(
        persistentStateType = PersistentMembershipState::class,
        contractStateType = MembershipState::class,
        strategies = [VaultaireDtoStrategyKeys.DEFAULT, VaultaireDtoStrategyKeys.LITE])
class MembershipStateMixin


/** Find the membership matching the given member and BNO parties */
fun <T : Any> getMembership(
        member: Party, networkId: String, cordaRpcService: CordaRpcService
): StateAndRef<MembershipState>? {
    val criteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
            .and(memberCriteria(member))
            .and(bnoCriteria(bno))
    val states = cordaRpcService.queryBy(MembershipState::class.java, criteria).states
    return if (states.isEmpty()) null
    else (states.sortedBy { it.state.data.modified }.last() as StateAndRef<MembershipState<T>>)
}

private fun memberCriteria(member: Party) =
        QueryCriteria.VaultCustomQueryCriteria(
                builder { PersistentMembershipState::cordaIdentity.equal(member) })
/*
private fun bnoCriteria(bno: Party) =
        QueryCriteria.VaultCustomQueryCriteria(
                builder { PersistentMembershipState::a.equal(bno) })
*/