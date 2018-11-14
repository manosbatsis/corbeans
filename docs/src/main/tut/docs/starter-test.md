---
layout: docs
title: "Starter Test"
---

# Spring-Boot Starter Test

The `corbeans-spring-boot-starter-test` depends on Junit5 and provides Corda network support for your automated tests.
The following sections how to quickly get started with corbeans in your project.

## Installation

To install the test starter, add the dependency to your build  using either the Gradle or Maven example bellow.

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
	// Note: you may need to exclude older junit deps in your con e.e. exclude group: 'junit', module: 'junit'
	testImplementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-starter-test:0.16'
}

```

### For Maven Users


Add the dependency in your Maven POM:

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter-test</artifactId>
	<version>0.16</version>
</dependency>
```
 
## With Driver Nodes

Extending `WithDriverNodesIT` allows creating a network per 'withDriverNodes' block, using the corbeans' 
config from `application.properties`. You may override the latter with an additional file in your test classpath, 
i.e. `src/test/resources/application.properties`:


```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MyWithDriverNodesIntegrationTest : WithDriverNodesIT() {

     // tell the driver which cordapp packages to load
     override fun getCordappPackages(): List<String> = listOf("net.corda.finance")

     @Test
     fun `Can create services`() {
         withDriverNodes {
             assertNotNull(this.services)
             assertTrue(this.services.keys.isNotEmpty())
         }
     }

     @Test
     fun `Can retrieve node identity`() {
         withDriverNodes {
             assertNotNull(services["partyANodeService"]?.myIdentity)
         }
     }

     @Test
     fun `Can retrieve notaries`() {
         withDriverNodes {
             assertNotNull(services["partyANodeService"]?.notaries())
         }
     }
}
```

## Implicit Network

Extending `WithImplicitNetworkIT` will automatically create and maintains a single Corda network throughout test execution,
agin using the corbeans' config from `application.properties`. You may override the latter with an
additional file in your test classpath, i.e. `src/test/resources/application.properties`.

Example:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MyWithSingleNetworkIntegrationTest : WithImplicitNetworkIT() {

     // tell the driver which cordapp packages to load
     override fun getCordappPackages(): List<String> = listOf("net.corda.finance")

     // autowire a service for a specific node
     @Autowired
     @Qualifier("partyANodeService")
     lateinit var service: CordaNodeService

     // autowire a unique-typed custom node service
     @Autowired
     lateinit var customCervice: SampleCustomCordaNodeServiceImpl

     @Test
     fun `Can inject services`() {
         assertNotNull(this.service)
         assertNotNull(this.customCervice)
     }

     @Test
     fun `Can retrieve node identity`() {
         assertNotNull(service.myIdentity)
     }
}
```