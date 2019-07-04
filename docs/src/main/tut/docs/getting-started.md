---
layout: docs
title: "Installation"
---

# Getting Started

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
	- [For Gradle Users](#for-gradle-users)
	- [For Maven Users](#for-maven-users)
- [Configuration](#configuration)
	- [Registered Beans](#registered-beans)
	- [Advanced Configuration](#advanced-configuration)
- [Autowiring Services](#autowiring-services)

<!-- /TOC -->

	> This guide is for manual installation. You can have a complete testable project in minutes using the [project template](project-template.html)  

The`corbeans-spring-boot-starter` module makes it easy for Spring Boot applications to interact with Corda networks. 
The starter reads the `application.properties` of your Spring Boot project and auto-configures Spring beans that 
expose Corda nodes via [RPC](https://docs.corda.net/clientrpc.html).

Those beans include a REST Controller, Service components and simple
RPC connection wrappers used to obtain a `CordaRPCOps` proxy for each Corda node.

The following sections how to quickly get started with corbeans in your project.

## Installation

To install the starter, add the dependency to your build  using either the Gradle or Maven example bellow.

> **Note:** to bundle a custom starter-based or the sample webserver with your node for use via CordForm and `runNodes`  
see the Sample Webserver documentation section:
[Configure for runNodes](http://127.0.0.1:4000/corbeans/docs/webserver.html#configure-for-runnodes).

### For Gradle Users

Corbeans is available in Maven central.

```groovy
repositories {
	mavenCentral()
	// OR, if changes have not yet been reflected to central:
	// maven { url "http://oss.sonatype.org/content/repositories/releases/" }
}
```

Add the starter dependency:

```groovy
dependencies {
	// For Corda 3.x. use Corbeans version 0.18 instead. 
	compile "com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:$corbeans_version"
	// Same as above for testing as well
	testCompile "com.github.manosbatsis.corbeans:corbeans-spring-boot-starter-test:$corbeans_version"
}
```

### For Maven Users

Add the dependency in your Maven POM:

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter</artifactId>
	<!-- For Corda 3.x. use Corbeans version 0.18 instead.-->
	<version>${corbeans_version}</version>
</dependency>
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter-test</artifactId>
	<!-- Same as above for testing as well.-->
	<version>${corbeans_version}</version>
	<scope>test</scope>
</dependency>
```

## Configuration

No configuration is required When using corbeans app as a drop-in replacement to corda-webserver within a node
folder.

For a node independent app, add nodes in your `application.properties` following the example bellow.
Use the party name for each node you want to the starter to register components for.
In this example we can see the minimal configuration required to create components nodes (parties) A and B:

```properties
# node for PartyA
corbeans.nodes.partyA.username=user1
corbeans.nodes.partyA.password=test
corbeans.nodes.partyA.address=localhost:10006

# node for PartyB
corbeans.nodes.partyB.username=user1
corbeans.nodes.partyB.password=test
corbeans.nodes.partyB.address=localhost:10009
```  

### Registered Beans

The auto-configuration will generate and register the following beans __per Corda Node__ 
based on the above `application.properties`:


Bean Type          | Name                     | Description
------------------ | ------------------------ | -------------------
NodeRpcConnection  | {nodeName}RpcConnection  | Node RPC connection and operations proxy
CordaNodeService   | {nodeName}NodeService    | A node-specific service component

It will also register the following network-level beans

Bean Type            | Name                     | Description
------------------   | ------------------------ | -------------------
CordaNetworkService  | {nodeName}NetworkService | Optionally used but convenient entry point to the cordbeans API 

> Starting with version 0.24, you need to explicitly add a default or custom controller to expose node operations, 
see [web-api](web-api.html) for details. 
