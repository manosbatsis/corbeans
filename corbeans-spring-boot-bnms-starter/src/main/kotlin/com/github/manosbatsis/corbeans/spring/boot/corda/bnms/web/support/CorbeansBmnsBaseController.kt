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
package com.github.manosbatsis.corbeans.spring.boot.corda.bnms.web.support

import com.github.manosbatsis.corbeans.spring.boot.corda.bnms.service.CordaBnmsService
import com.github.manosbatsis.corbeans.spring.boot.corda.config.CordaNodesProperties
import com.github.manosbatsis.corbeans.spring.boot.corda.web.CorbeansBaseController
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import java.util.Optional

open class CorbeansBmnsBaseController : CorbeansBaseController() {

    companion object {
        private val logger = LoggerFactory.getLogger(CorbeansBmnsBaseController::class.java)
    }

    /** Load config from application.properties */
    @Autowired
    lateinit var cordaNodesProperties: CordaNodesProperties


    val bnmsServiceType: Class<out CordaBnmsService<*>> by lazy {
        val typeName = cordaNodesProperties.bnmsServiceType
                ?: error("A corbeans.nodes.bnmsServiceType property is required")
        try {
            Class.forName(typeName) as Class<out CordaBnmsService<*>>
        }
        catch (e: Exception){throw RuntimeException(e)}
    }

    /** Store the default node name */
    protected lateinit var defaultNodeName: String

    /**
     * Get a BMNS service by node name. Default is either the only node name if single,
     * or `cordform` based on node.conf otherwise
     */
    protected fun getBmnsService(optionalNodeName: Optional<String>): CordaBnmsService<*> {
        return networkService.getService(bnmsServiceType, optionalNodeName)
    }

}
