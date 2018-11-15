---
layout: docs
title: "Sample Webserver"
---

# Sample Webserver


The `corbeans-corda-webserver` example project implements an alternative to Corda's default
Node webserver using modules of corbeans. The server exposes basic endpoints by default but supports multiple nodes
per instance, see bellow for a configuration example.

## Running the Server

There are multiple ways to let Spring Boot know about your Corda nodes using 
[externalized configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html). 
For a simple example, consider an `application.properties` file like:

```properties
spring.jackson.serialization.fail-on-empty-beans=false
springfox.documentation.swagger.v2.path=/api-docs

corbeans.nodes.partyA.username=user1
corbeans.nodes.partyA.password=test
corbeans.nodes.partyA.lazy=true
corbeans.nodes.partyA.address=localhost:10006


corbeans.nodes.partyB.username=user1
corbeans.nodes.partyB.password=test
corbeans.nodes.partyB.lazy=true
corbeans.nodes.partyB.address=localhost:10009
corbeans.nodes.partyB.primaryServiceType=com.github.manosbatsis.corbeans.corda.webserver.components.SampleCustomCordaNodeServiceImpl

logging.level.root=INFO
logging.level.com.github.manosbatsis=DEBUG
logging.level.net.corda=INFO
```
 
You can pass that file to the webserver executable with:

```bash
java -jar corbeans-corda-webserver-0.17.jar  --spring.config.location=/path/to/application.properties
```

For more alternatives see [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
in Spring Boot docs.


## API Reference

You can browse a running server's [swagger-ui](http://localhost:8080/swagger-ui.html) to view documentation of the 
available endpoints:

<img src="/corbeans/img/corda-webserver-spring-boot-swagger.png" alt="Corda Webserver Boot'sSwagger UI" />
