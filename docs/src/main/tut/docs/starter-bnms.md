---
layout: docs
title: "BNMS Starter"
---

# BNMS Starter

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
- [Configuration](#configuration)
	- [Custom Metadata Class](#custom-metadata-class)
	- [BNMS Service Implementation](#bnms-service-implementation)
	- [Nodes Configuration](#nodes-configuration)
- [Endpoints](#endpoints)

<!-- /TOC -->

The `corbeans-spring-boot-bnms-starter` provides autoconfigured, RESTful
BNMS services for members and BNOs. The services API provides operations for both
members and BNOs, e.g.:

- List business network members
- Request a membership or amend own membership's metadata
- Activate or suspend membership (for BNO nodes)

This document describes how to use the starter and quickly implement BNMS
services for your Spring Boot app.

## Installation

Corbeans is available in Maven central. To install the test starter, add the dependency to your build:

```groovy
repositories {
	mavenCentral()
	// OR, if changes have not yet been reflected to central:
	// maven { url "http://oss.sonatype.org/content/repositories/releases/" }

	// Optional: control the BNMS dependency versions
	compile "com.r3.businessnetworks:membership-service:$corda_solutions_version"
        compile "com.r3.businessnetworks:membership-service-contracts-and-states:$corda_solutions_version"
}
```

Add the BNMS starter dependency:

```groovy
dependencies {
    // Corbeans starter
    compile ("com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:$corbeans_version")
    // Corbeans BNMS starter
    compile ("com.github.manosbatsis.corbeans:corbeans-spring-boot-bnms-starter:$corbeans_version")
}

```

## Configuration

The following steps are required to properly configure and use the BNMS starter.

- Create your custom membership metadata class
- Implement a custom BNMS service

The sections bellow describe the steps in detail.


### Custom Metadata Class

First of all you need to create a metadata class that fits your needs.

`MembershipState` allows the use of a custom type as
the value of it's `membershipMetadata` member. It's worth noting the type
is also used by [com.r3.businessnetworks.membership.flows.GenericsUtilsKt#getAttachmentIdForGenericParam(com.r3.businessnetworks.membership.states.MembershipState<? extends java.lang.Object>)]
to to find the appropriate class location and turn it into an attachment.

Here's a sample implementation that adds a simple comment metadatum:

```kotlin
@CordaSerializable
data class MyCustomMembershipMetadata(
        val comment: String
        // Add more custom members here
)
```

> Your metadata class belongs to your cordapp module

### BNMS Service Implementation

To integrate your custom metadata class with Corbeans, implement a `CordaBnmsService`
by extending `AbstractCordaBnmsServiceImpl` to provide a mapping function between a `JsonNode`
and your custom metadata class:

```kotlin
/** BNMS service implementation */
class MyCustomBnmsService(
        nodeRpcConnection: NodeRpcConnection
): AbstractCordaBnmsServiceImpl<MyCustomMembershipMetadata>(nodeRpcConnection) {

    /** Convert the given JSON node to the target [MyCustomMembershipMetadata] instance */
    override fun toMembershipMetadata(jsonNode: JsonNode?): MyCustomMembershipMetadata {
        val givenComment = // read from JSON...
        return MyCustomMembershipMetadata(comment = givenComment)
    }
}
```

### Nodes Configuration

Last but not least, specify the custom BNMS Service implementation
using the `bnmsServiceType` slot for each of your nodes in _application.properties_.

```properties
# Common node config properties...
corbeans.nodes.partyA.username=user1
corbeans.nodes.partyA.password=test
corbeans.nodes.partyA.address=localhost:10006
corbeans.nodes.partyA.adminAddress=localhost:10046

# Custom BNMS service type for this node
corbeans.nodes.partyA.bnmsServiceType=mypackage.MyCustomBnmsService
```

Corbeans will create and register the appropriate BNMS service beans.

## Endpoints

 To view the BNMS endpoints created by corbeans, browse your Swagger e.g. http://localhost/swagger-ui.html

 <img src="/corbeans/img/bnms-swagger.png" alt="BNMS Endpoints in Swagger UI" />
