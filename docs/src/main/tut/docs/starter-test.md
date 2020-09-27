---
layout: docs
title: "Starter Test"
---

# Testing

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
	- [For Gradle Users](#for-gradle-users)
	- [For Maven Users](#for-maven-users)
- [With Driver Nodes](#with-driver-nodes)
- [Implicit Network](#implicit-network)
- [With CorbeansSpringExtension](#with-corbeansspringextension)
	- [Nested Tests](#nested-tests)
- [Cordapp Config](#cordapp-config)

<!-- /TOC -->

The `corbeans-spring-boot-starter-test` depends on Junit5 and provides Corda network support for your automated tests.
The following sections how to quickly get started with corbeans in your project.

## Installation

To install the test starter, add the dependency to your build  using either the Gradle or Maven example bellow.

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
	// Note: you may need to exclude older junit deps in your con e.e. exclude group: 'junit', module: 'junit'
	testImplementation "com.github.manosbatsis.corbeans:corbeans-spring-boot-starter-test:$corbeans_version"
}

```

### For Maven Users


Add the dependency in your Maven POM:

```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-starter-test</artifactId>
	<version>${corbeans_version}</version>
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

> __Deprecated__: Consider using CorbeansSpringExtension instead (see next section) as it starts nodes and the container in the correct order. 

Extending `WithImplicitNetworkIT` will automatically create and maintain a single Corda network throughout test 
execution, using the corbeans' config from `application.properties`. You may override the latter with an
additional file in your test classpath, i.e. `src/test/resources/application.properties`.

Example:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
class MyWithSingleNetworkIntegrationTest : WithImplicitNetworkIT() {

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

## With CorbeansSpringExtension

An alternative to extending `WithImplicitNetworkIT` is to use `CorbeansSpringExtension`. This will also  
automatically create and maintain a single Corda network throughout test class execution,
again using the corbeans' config from `application.properties`. You may override the latter with an
additional file in your test classpath, i.e. `src/test/resources/application.properties`.

Example:

```kotlin
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
class MyWithSingleNetworkIntegrationTest {
	// Same members as in the previous section
}
```
 

### Nested Tests

Some times you may want to speed up the build by using a single network throughout all tests.
You can do that using a single main test with `CorbeansSpringExtension` while nesting actual 
tests to structure properly via separate class files.

```kotlin
import com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate

/**
 * Root integration test suite. Launches a complete, preconfigured 
 * Corda network and RESTful service using [SpringBootTest] and [CorbeansSpringExtension], 
 * based on _cordapp-*_ and _server_ project modules.
 *
 * The actual "Foo" and "Bar" tests are structured as nested classes, allowing reuse the 
 * same network and webapp instance.
 *
 * If your tests require [Autowired] components, add them here and pass them via constructors.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
// Note we are using CorbeansSpringExtension Instead of SpringExtension
@ExtendWith(CorbeansSpringExtension::class)
class MainIntegrationTest {

    @Autowired
    lateinit var restTemplate: TestRestTemplate

    @Nested
    inner class `Foo tests` : FooTests(restTemplate)

    @Nested
    inner class `Bar tests` : BarTests(restTemplate)
}
```


## Cordapp Config

You can add custom cordapp configurations for a target package 
to be used while testing. By default `NodeDriverHelper.buildCordappConfig` 
will look for a `${cordappPackage}.config.properties` file in the classpath 
and, if found, will use it as config for the corresponding `TestCordapp` 
instance.