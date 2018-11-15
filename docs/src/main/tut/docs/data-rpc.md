---
layout: docs
title: "Data RPC"
---

# Data RPC

The `corbeans-spring-boot-rpc-datasource-starter` auto-configures JPA entities and repositories 
for your datasource that are compatible with the  
[RPC datasource schema](https://docs.corda.net/clientrpc.html?highlight=rpc#rpc-security-management).
 
For more advanced use-cases you can use `corbeans-spring-data-corda-rpc` instead and extend the included 
mapped superclasses as desired.

The following sections how to quickly get started with either in your project. The examples assume you have already 
configured a datasource in your project and have added dependencies a database driver and `org.springframework.boot:spring-boot-starter-data-jpa`.

## Installation

To install the desired module, add the dependency to your build  using either the Gradle or Maven example bellow.


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
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-rpc-datasource-starter:0.17'
	// Or, to use the JPA mapped superclasses only:
	// implementation 'com.github.manosbatsis.corbeans:corbeans-spring-data-corda-rpc:0.17'
}
```

### For Maven Users

Add the dependency in your Maven POM:


```xml
<dependency>
	<groupId>com.github.manosbatsis.corbeans</groupId>
	<artifactId>corbeans-spring-boot-rpc-datasource-starter</artifactId>
	<!-- Or, to use the JPA mapped superclasses only:
		<artifactId>corbeans-spring-data-corda-rpc</artifactId>
	-->
	<version>0.17</version>
</dependency>
```


## Use in your components:

Assuming you are using the starter:

```kotlin
// autowire RPC repos
@Autowired lateinit var rpcUserRepository: RpcUserRepository
@Autowired lateinit var rpcPermissionRepository: RpcPermissionRepository
@Autowired lateinit var rpcRoleRepository: RpcRoleRepository

// Use repos in your code, e.g.
val user = rpcUserRepository.save(RpcUser("user1", "user1", emptyList()))
```