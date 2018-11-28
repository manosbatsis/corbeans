/**
 *     Corda-Spring: integration and other utilities for developers working with Spring-Boot and Corda.
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
package com.github.manosbatsis.corbeans.spring.boot.corda.config.cordform

import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationEnvironmentPreparedEvent
import org.springframework.boot.system.ApplicationHome
import org.springframework.context.ApplicationListener
import org.springframework.core.env.PropertiesPropertySource
import java.io.File
import java.util.*

/**
 * Looks for Corda node/web-server.conf files within the same folder
 * as the running spring boot application JAR and, if found, applies them
 * to the application environment as corbeans configuration for the node
 */
class NodeConfCheckingListener : ApplicationListener<ApplicationEnvironmentPreparedEvent> {

    companion object {
        private val logger = LoggerFactory.getLogger(NodeConfCheckingListener::class.java)
    }

    /**
     * Handle an application event
     * @param event the event to respond to
     */
    override fun onApplicationEvent(event: ApplicationEnvironmentPreparedEvent) {
        // Get boot jar directory
        val homeDir: File = ApplicationHome().dir
        // Load node config if it exists
        val nodeConfFile = File(homeDir, "node.conf")
        if(nodeConfFile.exists()) {
            logger.debug("Found Corda node configuration at {}", nodeConfFile.absolutePath)
            val nodeConfig = buildNodeConfig(nodeConfFile)
            val nodeProperties = buildNodeProperties(nodeConfig)
            // Add node properties to env
            event.environment.propertySources.addFirst(PropertiesPropertySource("node", nodeProperties))
        }
        else {
            logger.debug("Not a Corda node directory: {}", homeDir.absolutePath)
        }
    }

    /**
     * Build node configuration based on `node.conf` and `web-server.conf`
     */
    private fun buildNodeProperties(finalConfig: Config): Properties {
        // Convert to spring boot properties
        val nodeProperties = Properties()

        // Set logs dir
        nodeProperties.put("logging.path", "logs")

        // Set webserver port
        val webAddress = getConfigRequiredString(finalConfig, "webAddress")
        nodeProperties.put("server.port", webAddress.substringAfterLast(':'))

        // Set RPC connection URLs
        nodeProperties.put("corbeans.nodes.default.lazy", true) // to work with `runNodes` script
        val rpcAddress = getConfigRequiredString(finalConfig, "rpcSettings.address")
        nodeProperties.put("corbeans.nodes.default.address", rpcAddress)
        nodeProperties.put("corbeans.nodes.default.adminAddress", getConfigRequiredString(finalConfig, "rpcSettings.adminAddress"))

        // Set RPC connection credentials
        var user: Config = selectRpcUser(finalConfig)
        nodeProperties.put("corbeans.nodes.default.username", getConfigRequiredString(user, "username"))
        nodeProperties.put("corbeans.nodes.default.password", getConfigRequiredString(user, "password"))

        val partyName = getConfigRequiredString(finalConfig, "myLegalName")
        logger.debug("Added corbeans config for party {}, RPC address: {}", partyName, rpcAddress)

        return nodeProperties
    }

    /**
     * Select an RPC user; one with ALL permissions if available,
     * the first found otherwise
     */
    private fun selectRpcUser(finalConfig: Config): Config {
        val users =
                finalConfig.getConfigList("security.authService.dataSource.users")
        if (users == null || users.isEmpty()) throw IllegalStateException("No RPC users are configured for node")
        // Use first user by default...
        var user: Config = users.first()
        // ... but try to get one with all permissions if available
        for (it in users) {
            val permissions = it.getStringList("permissions")
            if (permissions.contains("ALL") || permissions.contains("all")) {
                user = it
                break
            }
        }
        return user
    }

    /**
     * Load node configuration
     */
    private fun buildNodeConfig(nodeConfFile: File): Config {
        // TODO: handle CMD options?
        // Load node configuration from FS
        val parseOptions = ConfigParseOptions.defaults()
        // From node.conf
        val nodeConfig = ConfigFactory.parseFile(nodeConfFile, parseOptions.setAllowMissing(false))
        // From web-server.conf
        val webConfig = ConfigFactory.parseFile(
                File(nodeConfFile.parent, "web-server.conf"), parseOptions.setAllowMissing(false))

        return webConfig.withFallback(nodeConfig).resolve()
    }

    private fun getConfigRequiredString(config: Config, path: String): String =
            config.getString(path)
                    ?: throw IllegalStateException("Property not found in node config files: $path")

}