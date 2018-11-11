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
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-boot-rpc-datasource-starter:VERSION'
	// Or, to use the JPA mapped superclasses only:
	// implementation 'com.github.manosbatsis.corbeans:corbeans-spring-data-corda-rpc:VERSION'
}
```

### For Maven Users

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
	<artifactId>corbeans-spring-boot-rpc-datasource-starter</artifactId>
	<!-- Or, to use the JPA mapped superclasses only:
		<artifactId>corbeans-spring-data-corda-rpc</artifactId>
	-->
	<version>VERSION</version>
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