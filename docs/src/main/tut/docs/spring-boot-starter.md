---
layout: docs
title: Spring-Boot Starter
---

# Spring-Boot Starter

Corda provides a client RPC library that allows you to interact with a running node. The starting point for the client 
library is the `CordaRPCClient` class, which provides a `start` method that returns a `CordaRPCConnection`.  
A `CordaRPCConnection` allows you to access an implementation of the `CordaRPCOps` interface with proxy in Kotlin or 
getProxy() in Java. More detail on this functionality is provided in the [RPC docs](https://docs.corda.net/clientrpc.html).

The Spring-Boot starter automatically creates and configures REST Controller, Service components that wrap such 
a `CordaRPCOps` proxy for each one of your Corda nodes. All you need is the starter dependency in your project and the 
node connection parameters in `application.properties`. The following sections will quickly quide you through 
installing and configuring the starter according to your your needs.

## Quick Howto

Here's how to use the starter in your Spring-Boot app.

### Add the Starter Dependency

Add the dependency to your build  using the Gradle or Maven example bellow.

#### For Gradle Users

Add jitpack to your project repositories:

```groovy
repositories {
	//...
	maven { url 'https://jitpack.io' }
}
```

Add the starter dependency replacing VERSION with the latest tag or `master-SNAPSHOT`

```groovy
dependencies {
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:VERSION'
}
```

#### For Maven Users

Add jitpack to your project repositories:

```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

Add the starter dependency replacing VERSION with the latest tag or `master-SNAPSHOT`

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter</artifactId>
	<version>VERSION</version>
</dependency>
```

### Application Config

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
The only requirement is that you have to extend `BaseCordaNodeServiceImpl` 
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

### Autowiring Services

Services created and/or configured by corbeans may be autowired 
as any other component, for example: 

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