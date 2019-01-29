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
package com.github.manosbatsis.corbeans.spring.boot.corda.model.info

import net.corda.core.identity.Party
import net.corda.core.utilities.NetworkHostAndPort

data class NodeInfo(
        val platformVersion: Int,
        val peerNames: List<String>,
        val peers: List<String>,

        val identity: Party,
        val identities: List<Party>,
        val notaries: List<Party>,

        val flows: List<String>,
        val addresses: List<NetworkHostAndPort>
)

data class NetworkInfo(
        val nodes: Map<String, NodeInfo>
)