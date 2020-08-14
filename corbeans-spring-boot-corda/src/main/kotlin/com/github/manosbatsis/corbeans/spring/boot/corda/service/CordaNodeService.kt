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

import com.github.manosbatsis.corda.rpc.poolboy.connection.NodeRpcConnection

/**
 *  RPC-based, node-specific service
 */
interface CordaNodeService : CordaRpcService {

    /**
     * Run some code with a [NodeRpcConnection] from the pool in-context
     */
    fun <A> withNodeRpcConnection(block: (NodeRpcConnection) -> A): A {
        return delegate.poolBoy.withConnection(block)
    }
}

/**
 *  RPC-based, accounts-aware node-specific service
 */
interface CordaAccountsAwareNodeService : CordaNodeService, CordaAccountsAwareRpcService {

}
