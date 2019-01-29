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
package com.github.manosbatsis.corbeans.spring.boot.corda.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

/**
 * Application properties configuration for one or multiple
 * Corda nodes. Sample config:
 *
 * ```properties
 * # The cordapp packages to scan for during tests
 * corbeans.cordapPackages=net.corda.finance
 *
 * # first node
 * corbeans.nodes.partyA.username=user1
 * corbeans.nodes.partyA.password=test
 * corbeans.nodes.partyA.eager=true
 * corbeans.nodes.partyA.address=localhost:10006
 * corbeans.nodes.partyA.adminAddress=localhost:10046
 *
 * # second node
 * corbeans.nodes.partyB.username=user1
 * corbeans.nodes.partyB.password=test
 * corbeans.nodes.partyB.eager=true
 * corbeans.nodes.partyB.address=localhost:10009
 * corbeans.nodes.partyB.adminAddress=localhost:10049
 * corbeans.nodes.partyB.primaryServiceType=com.github.manosbatsis.corbeans.corda.webserver.components.SampleCustomCordaNodeServiceImpl
 *
 * # more nodes...
 *
 * # logging config etc.
 * logging.level.root=INFO
 * logging.level.com.github.manosbatsis=DEBUG
 * logging.level.net.corda=INFO
 * ```
 *
 * In the case of CordForm/runnodes an instance of this class is initialised by `NodeConfCheckingListener`
 * (see `corbeans-spring-boot-corda` module)
 *
 * @see com.github.manosbatsis.corbeans.spring.boot.corda.config.cordform.NodeConfCheckingListener
 */
@Component("cordaNodesProperties")
@ConfigurationProperties(prefix = "corbeans")
class CordaNodesProperties {
    var cordapPackages: List<String> = mutableListOf()
    var nodes: Map<String, NodeParams> = mutableMapOf()
    var objectMapper: ObjectMapperProperties = ObjectMapperProperties()
    var notarySpec: TestNotaryProperties = TestNotaryProperties()
    override fun toString(): String {
        return "CordaNodesProperties(cordapPackages=$cordapPackages, nodes=$nodes, objectMapper=$objectMapper, notarySpec=$notarySpec)"
    }


}
