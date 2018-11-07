---
layout: docs
title: "Data RPC"
---

The `corbeans-spring-data-corda-rpc` module provides JPA entities that are compatible with the 
[RPC datasource schema](https://docs.corda.net/clientrpc.html?highlight=rpc#rpc-security-management) 
and suitable for applications using JPA to access such a database directly.
The following sections how to quickly get started with corbeans in your project.

## Installation

To install the starter, add the dependency to your build  using either the Gradle or Maven example bellow.

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
	implementation 'com.github.manosbatsis.corbeans:corbeans-spring-data-corda-rpc:VERSION'
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
	<artifactId>corbeans-spring-data-corda-rpc</artifactId>
	<version>VERSION</version>
</dependency>
```