# Spring-Corda Integration [![Release](https://jitpack.io/v/com.github.manosbatsis/corda-spring.svg)](https://jitpack.io/#com.github.manosbatsis/corda-spring)



This project provides integration and other utilities for developers working with  
[Spring-Boot](https://spring.io/projects/spring-boot) and [Corda](https://www.corda.net/).

This project is perfectly usable but far from any notion of being 'complete'. Please browse
the [current issues](https://github.com/manosbatsis/corda-spring/issues) to get an idea of
the work in progress and post any questions or suggestions you may have.  

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Spring-Boot Starter](#spring-boot-starter)
	- [Sample Configuration](#sample-configuration)
		- [Starter Dependency](#starter-dependency)
			- [Gradle Users](#gradle-users)
			- [Maven Users](#maven-users)
		- [Application Config](#application-config)
		- [Registered Beans](#registered-beans)
		- [Advanced Configuration](#advanced-configuration)
- [Corda Webserver Boot](#corda-webserver-boot)

<!-- /TOC -->
## Spring-Boot Starter

The starter autoconfiguration provides effortless bootstrapping of
REST Controller and Service components that expose Corda Nodes via RPC.

### Sample Configuration

Here's how to use the starter in your Spring-Boot app.

#### Starter Dependency

Add the dependency to your build replacing VERSION with the latest tag or
`master-SNAPSHOT`

##### Gradle Users

Add jitpack to your project repositories:

```groovy
allprojects {
	repositories {
		//...
		maven { url 'https://jitpack.io' }
	}
}
```

Add the starter dependency

```groovy
dependencies {
	implementation 'com.github.manosbatsis.corda-spring:spring-boot-corda-starter:VERSION'
}
```

##### Maven Users

Add jitpack to your project repositories:

```xml
<repositories>
	<repository>
		<id>jitpack.io</id>
		<url>https://jitpack.io</url>
	</repository>
</repositories>
```

Add the starter dependency

```xml
<dependency>
	<groupId>com.github.manosbatsis.corda-spring</groupId>
	<artifactId>spring-boot-corda-starter</artifactId>
	<version>VERSION</version>
</dependency>
```

#### Application Config

Add nodes in your `application.properties` following the example bellow.
Use the party name for each node you want to the starter to register components for.
In this example, we create components for nodes "PartyA", "PartyB" and "PartyC":

```properties
# node for PartyA
spring-corda.nodes.PartyA.username=user1
spring-corda.nodes.PartyA.password=test
spring-corda.nodes.PartyA.address=localhost:10006
spring-corda.nodes.PartyA.adminAddress=localhost:10046

# node for PartyB
spring-corda.nodes.PartyB.username=user1
spring-corda.nodes.PartyB.password=test
spring-corda.nodes.PartyB.address=localhost:10009
spring-corda.nodes.PartyB.adminAddress=localhost:10049

# node for PartyC
spring-corda.nodes.PartyC.username=user1
spring-corda.nodes.PartyC.password=test
spring-corda.nodes.PartyC.address=localhost:10012
spring-corda.nodes.PartyC.adminAddress=localhost:10052
```  

#### Registered Beans

The auto configuration will register the following beans per Corda Node added in the application config:


Bean Type          | Name                     | Description
------------------ | ------------------------ | -------------------
NodeRpcConnection  | {nodeName}RpcConnection  | Provides an Node RPC connection proxy
CordaNodeService   | {nodeName}NodeService     | A Node Service Bean

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
GET    | /nodes/{nodeName}/states                | Returns a list of flow classnames

#### Advanced Configuration

Custom service types:

```properties
# node for PartyA
# ...
spring-corda.nodes.PartyA.serviceType=my.subclass.of.CordaNodeServiceImpl
```  

## Corda Webserver Boot

The corda-webserver-spring-boot module implements a replacement to Corda's default
Node webserver. Run the server passing command line arguments to configured the nodes
and browse [/swagger-ui.html](http://localhost:8080/swagger-ui.html) to view the available endpoints documentation
generated by swagger.

<img src ="etc/img/corda-webserver-spring-boot-swagger.png" alt="Corda Webserver Boot'sSwagger UI" />
