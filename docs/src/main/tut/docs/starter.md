---
layout: docs
title: "Spring-Boot Starter"
---

# Spring-Boot Starter

The`corbeans-spring-boot-starter` module makes it easy for Spring Boot applications to interact with Corda networks. The starter 
reads your application.properties and auto-configures Spring components to expose Corda nodes via 
[RPC](https://docs.corda.net/clientrpc.html). 

The components include REST Controller, Service components and simple 
RPC connection wrappers used to obtain a `CordaRPCOps` proxy for each Corda node. 
 
The following sections how to quickly get started with corbeans in your project.

## Installation

To install the starter, add the dependency to your build  using either the Gradle or Maven example bellow.

### For Gradle Users

Corbeans is available in Maven central.

```groovy
repositories {
	mavenCentral()
}
```

Add the starter dependency:

```groovy
dependencies {
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:0.17'
}
```

### For Maven Users

Add the dependency in your Maven POM:

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter</artifactId>
	<version>0.17</version>
</dependency>
```

## Configuration

Add nodes in your `application.properties` following the example bellow.
Use the party name for each node you want to the starter to register components for.
In this example, we create components for nodes e.g.  "PartyA" and, "PartyB":

```properties
# node for PartyA
corbeans.nodes.PartyA.username=user1
corbeans.nodes.PartyA.password=test
corbeans.nodes.PartyA.address=localhost:10006

# node for PartyB
corbeans.nodes.PartyB.username=user1
corbeans.nodes.PartyB.password=test
corbeans.nodes.PartyB.address=localhost:10009
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

### Advanced Configuration

You can instruct corbeans to create and register your custom service implementations. 
The only requirement is that you have to extend `CordaNodeServiceImpl` 
(or `CordaNodeServiceImpl` and so on), for example:

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

Then instruct corbeans to use your custom service type via the 
`primaryServiceType` property for the desired node:

```properties
# node for PartyB
# ...
corbeans.nodes.PartyA.primaryServiceType=my.subclass.of.CordaNodeServiceImpl
```  

## Autowiring Services

Service beans registered by corbeans may be autowired as any other component, for example: 

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