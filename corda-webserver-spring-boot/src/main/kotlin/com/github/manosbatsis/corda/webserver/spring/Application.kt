package com.github.manosbatsis.corda.webserver.spring

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication


/**
 * Our Spring Boot application.
 */
//@EnableAutoConfiguration
@SpringBootApplication
class Application

    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }
