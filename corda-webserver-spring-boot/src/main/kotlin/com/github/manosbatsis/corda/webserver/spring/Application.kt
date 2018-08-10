package com.github.manosbatsis.corda.webserver.spring

import com.fasterxml.jackson.databind.SerializationFeature
import net.corda.client.jackson.JacksonSupport
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder


/**
 * Our Spring Boot application.
 */
//@EnableAutoConfiguration
@SpringBootApplication
class Application


    fun main(args: Array<String>) {
        runApplication<Application>(*args)
    }
