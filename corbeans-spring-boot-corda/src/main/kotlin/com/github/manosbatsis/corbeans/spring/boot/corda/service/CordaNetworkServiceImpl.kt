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
    }

    protected lateinit var defaultNodeName: String

    /** Node services by configured name */
    @Autowired
    override lateinit var nodeServices: Map<String, CordaNodeService>

    @PostConstruct
    fun postConstruct() {
        // if single node config, use the only node name as default, else reserve explicitly for cordform
        defaultNodeName = if (nodeServices.keys.size == 1) nodeServices.keys.first()
        else NodeParams.NODENAME_CORDFORM
        logger.debug("Auto-configured RESTful services for Corda nodes:: {}, default node: {}",
                nodeServices.keys, defaultNodeName)
    }

    /**
     * Get a Node service by name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    override fun getNodeService(optionalNodeName: Optional<String>): CordaNodeService {
        val nodeName = if (optionalNodeName.isPresent) optionalNodeName.get() else defaultNodeName
        return this.nodeServices.get("${nodeName}NodeService")
                ?: throw IllegalArgumentException("Node not found: $nodeName")
    }
}