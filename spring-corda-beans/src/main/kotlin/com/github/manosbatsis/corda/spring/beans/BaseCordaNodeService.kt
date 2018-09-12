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
package com.github.manosbatsis.corda.spring.beans

import net.corda.core.identity.Party
import java.time.LocalDateTime

/**
 *  Basic interface for RPC-based node services
 */
interface BaseCordaNodeService {

    /** Get the node identity */
    val myIdentity: Party

    /** Returns a list of the node's network peers. */
    fun peers(): Map<String, List<String>>


    /** Returns a list of the node's network peer names. */
    fun peerNames(): Map<String, List<String>>

    fun serverTime(): LocalDateTime

}
