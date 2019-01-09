---
layout: docs
title: "Sample Webserver"
---

# Sample Webserver

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Download](#download)
- [Running the Server](#running-the-server)
	- [Configure for `runNodes`](#configure-for-runnodes)
	- [Configure for Multiple Nodes](#configure-for-multiple-nodes)
	- [Optional Properties](#optional-properties)
	- [Run](#run)
- [API Reference](#api-reference)

<!-- /TOC -->

The `corbeans-corda-webserver` example project implements an alternative to Corda's default
Node webserver using modules of corbeans. The server exposes basic endpoints by default but supports either a single
node via `runNodes` or multiple manually configured nodes, see bellow for a configuration examples.

## Download

You can use your own custom webserver based on the [starter](getting-started.html) or download the sample at
https://oss.sonatype.org/content/repositories/releases/com/github/manosbatsis/corbeans/corbeans-corda-webserver/

## Running the Server

Spring Boot applications using the [starter](getting-started.html) can be run either manually or via `runNodes` as a
drop-in replacement to the corda-webserver.



### Configure for `runNodes`

To use with `runNodes`, add your custom or sample webserver JAR in the node folder and renaming the file to replace
the default *corda-webserver.jar*:

<pre>
├── nodes
│   ├── BankA
│   │   ├── additional-node-infos
│   │   ├── certificates
│   │   ├── corda.jar
│   │   ├── cordapps
│   │   ├── <b>corda-webserver.jar</b>
│   │   ├── drivers
│   │   ├── logs
│   │   ├── network-parameters
│   │   ├── node.conf
│   │   ├── nodeInfo-4B67...
│   │   ├── persistence.mv.db
│   │   ├── persistence.trace.db
│   │   └── web-server.conf
│   ├── BankA_node.conf
│   ├── BankA_web-server.conf
│   ├── Notary Service
│   ├── Notary Service_node.conf
│   ├── runnodes
│   ├── runnodes.bat
│   └── runnodes.jar
</pre>

With Corda and `cordformation` Gradle plugin versions 4 and 4.0.25 and up respectively, you can automate this by setting the `webserverJar` property of
the `CordForm` plugin, by applyihng the following changes in your cordapp build:

```groovy
// 1: Add proper cordformation and quasar plugin versions
buildscript {
    dependencies {
        classpath "net.corda.plugins:cordformation:$corda_gradle_plugins_version"
        classpath "net.corda.plugins:quasar-utils:$corda_gradle_plugins_version"
        //...
    }
}
// 2: Add Corda v4
dependencies {
    cordaCompile "net.corda:corda-jackson:$corda_release_version"
    cordaCompile "net.corda:corda-rpc:$corda_release_version"
    cordaCompile "net.corda:corda-core:$corda_release_version"
    cordaRuntime "net.corda:corda:$corda_release_version"
}

// 3: Add webserverJar to each node
task deployNodes(type: net.corda.plugins.Cordform, dependsOn: ['jar']) {
    //...
    node {
        name "O=BankA,L=London,C=GB"
        p2pPort 10005
        cordapps = []
        rpcUsers = ext.rpcUsers
        rpcSettings {
            port 10007
            adminPort 10008
        }
        webPort 10009
        webserverJar  "/PATH/TO/MY/corbeans-corda-webserver.jar"
    }

}
```

Then use `runnodes` as usual to see the corbeans app in place of the original corda wobserver:


<img src="/corbeans/img/runnodes.png" alt="runnodes xterm shells" />



### Configure for Multiple Nodes

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

### Optional Properties

The following properties are optional per node (e.g. partA) 
with defaults taken from `CordaRPCClientConfiguration.DEFAULT`

```properties
#corbeans.nodes.partyA.connectionMaxRetryInterval=
#corbeans.nodes.partyA.connectionRetryInterval=
#corbeans.nodes.partyA.connectionRetryIntervalMultiplier=
#corbeans.nodes.partyA.deduplicationCacheExpiry=
#corbeans.nodes.partyA.maxFileSize=
#corbeans.nodes.partyA.maxReconnectAttempts=
#corbeans.nodes.partyA.minimumServerProtocolVersion=
#corbeans.nodes.partyA.observationExecutorPoolSize=
#corbeans.nodes.partyA.reapInterval=
#corbeans.nodes.partyA.trackRpcCallSites=
```

### Run

You can pass that file to the webserver executable with:

```bash
java -jar corbeans-corda-webserver-VERSION.jar  --spring.config.location=/path/to/application.properties
```

For more alternatives see [Externalized Configuration](https://docs.spring.io/spring-boot/docs/current/reference/html/boot-features-external-config.html)
in Spring Boot docs.


## API Reference

You can browse a running server's [swagger-ui](http://localhost:8080/swagger-ui.html) to view documentation of the
available endpoints:

<img src="/corbeans/img/corda-webserver-spring-boot-swagger.png" alt="Corda Webserver Boot'sSwagger UI" />
