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
package com.github.manosbatsis.corbeans.corda.webserver.components

import net.corda.core.crypto.SecureHash
import net.corda.core.identity.CordaX500Name
import org.apache.logging.log4j.LogManager
import org.springframework.web.bind.annotation.*
import springfox.documentation.annotations.ApiIgnore

/**
 * Used for conversion/formatter testing
 */
@RestController
@ApiIgnore
@RequestMapping("api/echo")
class EchoController {

    class EchoModel() {
        var secureHash: SecureHash? = null
        var cordaX500Name: CordaX500Name? = null
    }

    companion object {
        private val logger = LogManager.getLogger(EchoController::class.java)
    }

    @GetMapping("echoSecureHash/{value}")
    fun echoSecureHash(@PathVariable value: SecureHash): SecureHash {
        logger.info("SecureHash: {}", value)
        return value
    }

    @GetMapping("echoCordaX500Name/{value}")
    fun echoCordaX500Name(@PathVariable value: CordaX500Name): CordaX500Name {
        logger.info("CordaX500Name: {}", value)
        return value
    }

    @PostMapping("echoModel")
    fun echoModel(value: EchoModel): EchoModel {
        logger.info("EchoModel: {}", value)
        logger.info("EchoModel secureHash: {}", value.secureHash)
        logger.info("EchoModel cordaX500Name: {}", value.cordaX500Name)
        return value
    }


}
