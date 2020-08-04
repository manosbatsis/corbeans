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

import com.github.manosbatsis.vaultaire.dto.info.ExtendedNodeInfo
import com.github.manosbatsis.vaultaire.dto.info.NetworkInfo
import java.util.Optional

/**
 *  Corda network service
 */
interface CordaNetworkService {

    /** The default Node name or null */
    val defaultNodeName: String?

    /** Available node names */
    val nodeNames: Set<String>

    /** Organization name to node names */
    val nodeNamesByOrgName: Map<String, String>

    /** X500 name to node names */
    val nodeNamesByX500Name: Map<String, String>

    /**
     * Resolve the given node name
     * @param optionalNodeName an optional config node name (e.g. "foo" when configured as `corbeans.nodes.foo`)
     * an organization name or a X500 name
     */
    fun resolveNodeName(optionalNodeName: Optional<String>): String

    /** Get information about known node network(s) and configuration */
    fun getInfo(): NetworkInfo

    /** Refresh the network map cache of every node registered in corbeans configuration */
    fun refreshNetworkMapCaches()

    /**
     * Get a Node service by name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    fun getNodeService(optionalNodeName: Optional<String> = Optional.empty()): CordaNodeService

    /**
     * Get a Node service by name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    fun getNodeService(nodeName: String?): CordaNodeService {
        return this.getNodeService(Optional.ofNullable(nodeName))
    }


    /**
     * Get a service of the given [serviceType] for the node corresponding
     * to the input name. Default is either the only node name
     * if single, or `cordform` based on node.conf otherwise
     */
    fun <T> getService(
            serviceType: Class<T>,
            optionalNodeName: Optional<String> = Optional.empty()
    ): T

    /**
     * Get a service of the given [serviceType] for the node corresponding
     * to the input name. Default is either the only node name if
     * single, or `cordform` based on node.conf otherwise
     */
    fun <T> getService(serviceType: Class<T>, nodeName: String?): T {
        return this.getService(serviceType, Optional.ofNullable(nodeName))
    }

    fun getNodesInfo(): Map<String, ExtendedNodeInfo>
}
