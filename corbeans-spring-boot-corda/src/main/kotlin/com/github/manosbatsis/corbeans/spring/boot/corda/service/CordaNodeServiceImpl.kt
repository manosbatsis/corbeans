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

import com.github.manosbatsis.corda.rpc.poolboy.PoolBoyConnection
import com.github.manosbatsis.vaultaire.plugin.accounts.service.node.AccountsAwareNodeServicePoolBoyDelegate
import com.github.manosbatsis.vaultaire.service.ServiceDefaults
import com.github.manosbatsis.vaultaire.service.SimpleServiceDefaults
import com.github.manosbatsis.vaultaire.service.node.NodeServiceRpcPoolBoyDelegate


/**
 *  Basic RPC-based node service implementation.
 *
 */
open class CordaNodeServiceImpl(
        override val delegate: NodeServiceRpcPoolBoyDelegate
) : CordaRpcServiceBase(delegate), CordaNodeService {

    /** [PoolBoyConnection]-based constructor */
    constructor(
            poolBoy: PoolBoyConnection, defaults: ServiceDefaults = SimpleServiceDefaults()
    ) : this(NodeServiceRpcPoolBoyDelegate(poolBoy, defaults))

}

/**
 *  Basic accounts-aware  RPC-based node service implementation.
 *
 */
open class CordaAccountsAwareNodeServiceImpl(
        override val delegate: AccountsAwareNodeServicePoolBoyDelegate
) : CordaAccountsAwareRpcServiceBase(delegate), CordaAccountsAwareNodeService {

    /** [PoolBoyConnection]-based constructor */
    constructor(
            poolBoy: PoolBoyConnection, defaults: ServiceDefaults = SimpleServiceDefaults()
    ) : this(AccountsAwareNodeServicePoolBoyDelegate(poolBoy, defaults))

}
