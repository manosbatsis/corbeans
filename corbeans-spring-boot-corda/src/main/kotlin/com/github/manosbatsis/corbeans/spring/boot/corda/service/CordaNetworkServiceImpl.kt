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
        private val SERVICE_NAME_SUFFIX = "NodeService"
    }

    override val defaultNodeName: String? by lazy {
        if (nodeServices.keys.size == 1)
            nodeServices.keys.single().replace(SERVICE_NAME_SUFFIX, "")
        else null
    }

    override val nodeNamesByOrgName by lazy {
        nodeServices.map {
            it.value.myIdentity.name.organisation to it.key.replace(SERVICE_NAME_SUFFIX, "")
        }.toMap()
    }

    override val nodeNamesByX500Name by lazy {
        nodeServices.map {
            it.value.myIdentity.name.toString() to it.key.replace(SERVICE_NAME_SUFFIX, "")
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

    override fun getInfo(): NetworkInfo {
        return NetworkInfo(getNodesInfo())
    }

    override fun getNodesInfo() = this.nodeServices
            .filterNot { it.value.skipInfo() } // skip?
            .map { it.key.substring(0, it.key.length - SERVICE_NAME_SUFFIX.length) to it.value.getInfo() }
            .toMap()

    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        val nodeName = resolveNodeName(optionalNodeName)
        logger.debug("getNodeService nodeName: ${nodeName}, node names: ${this.nodeServices}, org names: ${this.nodeNamesByOrgName}")
        return this.nodeServices.get("${nodeName}NodeService")
                ?: throw IllegalArgumentException("Node not found for name: ${optionalNodeName.orElse(null)}, resolved: $nodeName")
    }

    override fun getNodeService(nodeName: String?): CordaNodeService {
        return this.getNodeService(Optional.ofNullable(nodeName))
    }

    override fun resolveNodeName(optionalNodeName: Optional<String>): String {
        var nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName ?: throw IllegalArgumentException("No nodeName was given and a default one is not available")
        if (nodeName.isBlank()) throw IllegalArgumentException("nodeName cannot be an empty or blank string")
        // If organization name match
        val resolved = if (nodeServices.containsKey("${nodeName}${SERVICE_NAME_SUFFIX}")) nodeName
        else if (nodeNamesByOrgName.containsKey(nodeName)) nodeNamesByOrgName.getValue(nodeName)
        else if (nodeNamesByX500Name.containsKey(nodeName)) nodeNamesByX500Name.getValue(nodeName)
        else if (nodeServices.containsKey("${lowcaseFirst(nodeName)}${SERVICE_NAME_SUFFIX}")) lowcaseFirst(nodeName)
        else throw IllegalArgumentException("Failed resolving node name: ${nodeName}, " +
                "available node names: ${this.nodeServices}, " +
                "available org names: ${this.nodeNamesByOrgName}, " +
                "available X500 names: ${this.nodeNamesByX500Name}")
        logger.debug("resolveNodeName, nodeName: $nodeName, resolved: $resolved")
        return resolved
    }

    override fun refreshNetworkMapCaches() = nodeServices.values.forEach{
        it.refreshNetworkMapCache()
    }

    private fun lowcaseFirst(s: String): String {
        val c = s.toCharArray()
        c[0] = Character.toLowerCase(c[0])
        return String(c)
    }
}
