# Spring-Corda Integration

This project provides utilities and integration for Corda Spring developers

## Spring-Boot Starter

The starter autoconfiguration provides effortless bootstrapping of 
REST Controller and Service components that expose Corda Nodes via RPC. 

### Sample Configuration

Here's how to use the starter in your Spring-Boot app.

#### Starter Dependency

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
	implementation 'com.github.manosbatsis.corda-spring:spring-boot-corda-starter:master-SNAPSHOT'
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
	<version>master-SNAPSHOT</version>
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

