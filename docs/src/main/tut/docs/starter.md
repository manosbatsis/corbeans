---
layout: docs
title: "Spring-Boot Starter"
---

# Spring-Boot Starter

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
	- [For Gradle Users](#for-gradle-users)
	- [For Maven Users](#for-maven-users)
- [Configuration](#configuration)
	- [Registered Beans](#registered-beans)
	- [Endpoints](#endpoints)
	- [Advanced Configuration](#advanced-configuration)
- [Autowiring Services](#autowiring-services)

<!-- /TOC -->

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
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:0.19'
}
```

### For Maven Users

Add the dependency in your Maven POM:

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter</artifactId>
	<!-- For Corda 3.x. use Corbeans version 0.18 instead.-->
	<version>0.19</version>
</dependency>
```

## Configuration

No configuration is needed When using corbeans app as a drop-in replacement to corda-webserver within a node
folder.

For a node independent app, add nodes in your `application.properties` following the example bellow.
Use the party name for each node you want to the starter to register components for.
In this example, we create components for nodes e.g.  parties A and B:

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

The auto-configuration will generate and register the following beans per Corda Node added in the application config:


Bean Type          | Name                     | Description
------------------ | ------------------------ | -------------------
NodeRpcConnection  | {nodeName}RpcConnection  | Provides an Node RPC connection proxy
CordaNodeService   | {nodeName}NodeService    | A Node Service Bean

### Endpoints

A controller is also added with endpoints exposing business methods for all
listing identity, network peers, notaries, flows, states, finding attachments etc.


Method | Path                                    | Description
------ | --------------------------------------- | -------------------
GET    | /nodes/{nodeName}/serverTime            | Return tbe node time in UTC
GET    | /nodes/{nodeName}/whoami                | Returns the Node identity's name
GET    | /nodes/{nodeName}/me                    | Returns the Node identity's x500Principal name
GET    | /nodes/{nodeName}/peers                 | Returns a list of the node's network peers
GET    | /nodes/{nodeName}/peersnames            | Returns a list of the node's network peer names
GET    | /nodes/{nodeName}/peersnames            | Returns a list of node's network peer names
GET    | /nodes/{nodeName}/attachment/{id}       | Returns the attachment mathing the given ID
GET    | /nodes/{nodeName}/addresses             | Returns a list of node addresses
GET    | /nodes/{nodeName}/notaries              | Returns a list of notaries in node's network
GET    | /nodes/{nodeName}/states                | Returns a list of states
GET    | /nodes/{nodeName}/flows                 | Returns a list of flow classnames

> **Note:** When corbeans parses node.conf as the configuration (i.e. when using `CordForm` and/or `runnodes`),
the corresponding base path for the default node endpoints is simply `node` instead of `nodes/{nodeName}`.

### Advanced Configuration

You can instruct corbeans to create and register your custom service implementations.
The only requirement is that you have to extend `CordaNodeServiceImpl`
(or otherwise implement `CordaNodeService`), for example in Kotlin:

```kotlin
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection

class SampleCustomCordaNodeServiceImpl(
        nodeRpcConnection: NodeRpcConnection
) : CordaNodeServiceImpl(nodeRpcConnection) {

    /** dummy method */
    fun dummy(): Boolean = true

}
```

or Java:


```java
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl;
import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection;

public class SampleCustomCordaNodeServiceImpl extends CordaNodeServiceImpl {

	public SampleCustomCordaNodeServiceImpl(NodeRpcConnection nodeRpcConnection){
		super(nodeRpcConnection);
	}
    /** dummy method */
    public Boolean dummy(){return true;}
}
```

Then instruct corbeans to use your custom service type via the
`primaryServiceType` property for the desired node:

```properties
# node for PartyB
# ...
corbeans.nodes.PartyA.primaryServiceType=my.subclass.of.CordaNodeServiceImpl
```  

## Autowiring Services

Service beans registered by corbeans may be autowired as any other component, for example in Kotlin:

```kotlin
    // autowire all created services, mapped by name
    @Autowired
    lateinit var services: Map<String, CordaNodeService>

    // autowire a services for a specific node
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService

    // autowire a unique custom service
    @Autowired
    lateinit var customCervice: SampleCustomCordaNodeServiceImpl
```

or Java

```java
    // autowire all created services, mapped by name
    @Autowired
    private Map<String, CordaNodeService> services;

    // autowire a services for a specific node
    @Autowired
    @Qualifier("partyANodeService")
    private CordaNodeService service;

    // autowire a unique custom service
    @Autowired
    private SampleCustomCordaNodeServiceImpl customCervice;
```
