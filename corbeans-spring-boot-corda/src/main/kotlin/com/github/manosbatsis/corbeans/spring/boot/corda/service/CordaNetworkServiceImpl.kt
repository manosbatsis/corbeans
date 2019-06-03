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

//import org.springframework.messaging.simp.SimpMessagingTemplate
import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
import com.github.manosbatsis.corbeans.spring.boot.corda.model.info.NetworkInfo
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.*
import javax.annotation.PostConstruct


/**
 * Default Corda network service
 */
open class CordaNetworkServiceImpl : CordaNetworkService {

    companion object {
        private val logger = LoggerFactory.getLogger(CordaNetworkServiceImpl::class.java)
        private val serviceNameSuffix = "NodeService"
    }

    override val defaultNodeName: String by lazy {
        if (nodeServices.keys.size == 1
                || !nodeServices.keys.contains(NodeParams.NODENAME_CORDFORM))
            nodeServices.keys.first().replace(serviceNameSuffix, "")
        else NodeParams.NODENAME_CORDFORM
    }

    override val nodeNamesByOrgName by lazy {
        nodeServices.keys.map {
            nodeServices.getValue(it).myIdentity.name.organisation to it.replace(serviceNameSuffix, "")
        }.toMap()
    }

    /** Node services by configured name */
    @Autowired
    override lateinit var nodeServices: Map<String, CordaNodeService>


    @PostConstruct
    fun postConstruct() {
        logger.debug("Auto-configured RESTful services for Corda nodes:: {}, default node: {}",
                nodeServices.keys, defaultNodeName)
    }

    /**
     * Get information about known node network(s) and configuration
     */
    override fun getInfo(): NetworkInfo {
        return NetworkInfo(getNodesInfo())
    }

    /**
     * Get information about known nodes
     */
    override fun getNodesInfo() = this.nodeServices
            .filterNot { it.value.skipInfo() } // skip?
            .map { it.key.substring(0, it.key.length - serviceNameSuffix.length) to it.value.getInfo() }
            .toMap()

    /**
     * Get a Node service by name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        val nodeName = resolveNodeName(optionalNodeName)
        return this.nodeServices.get("${nodeName}NodeService")
                ?: throw IllegalArgumentException("Node not found for name: ${optionalNodeName.orElse(null)}, resolved: $nodeName")
    }

    /**
     * Get a Node service by name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    override fun getNodeService(nodeName: String?): CordaNodeService {
        return this.getNodeService(Optional.ofNullable(nodeName))
    }

    protected fun resolveNodeName(optionalNodeName: Optional<String>): String {
        var nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
        if (nodeName.isBlank()) throw IllegalArgumentException("nodeName cannot be an empty or blank string")
        // If organization name match
        return if (nodeNamesByOrgName.containsKey(nodeName)) nodeNamesByOrgName.getValue(nodeName)
        else lowcaseFirst(nodeName)
    }

    private fun lowcaseFirst(s: String): String {
        val c = s.toCharArray()
        c[0] = Character.toLowerCase(c[0])
        return String(c)
    }
}
