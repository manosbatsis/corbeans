---
layout: docs
title: "BNMS Starter"
---

# BNMS Starter

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Installation](#installation)
	- [Corda BNMS Repository](#corda-bnms-repository)
	- [Cordapp Dependencies](#cordapp-dependencies)
	- [Spring Boot Application Dependencies](#spring-boot-application-dependencies)
- [Configuration](#configuration)
	- [Custom Metadata Class](#custom-metadata-class)
	- [BNMS Service Implementation](#bnms-service-implementation)
	- [Nodes Configuration](#nodes-configuration)
- [Endpoints](#endpoints)

<!-- /TOC -->

The `corbeans-spring-boot-bnms-starter` provides autoconfigured, RESTful
BNMS services for Corda's [memberships-management](https://github.com/manosbatsis/corda-solutions/tree/master/bn-apps/memberships-management)
cordapp. The services expose operations for both members and BNOs, e.g.:

- List business network members
- Request a membership or amend own membership's metadata
- Activate or suspend membership (for BNO nodes)

This document describes how to use the starter and quickly implement BNMS
services for your Spring Boot app.

## Installation

### Corda BNMS Repository

Make sure to add the corda-solutions-releases repository to your build modules.

```groovy
	// Corda BNMS repo
	maven { url 'https://ci-artifactory.corda.r3cev.com/artifactory/corda-solutions-releases'}
```

### Cordapp Dependencies

Add the Corda BNMS dependencies to your cordapp:

```groovy

dependencies {
	// Corda memberships-management deps
    compile "com.r3.businessnetworks:membership-service:$corda_solutions_version"
    compile "com.r3.businessnetworks:membership-service-contracts-and-states:$corda_solutions_version"
    //...
}
```

### Spring Boot Application Dependencies


Corbeans is available in Maven central. To install the main and BNMS starters, add the
following dependencies to your build.


```groovy
dependencies {
    // Corbeans starter
    compile ("com.github.manosbatsis.corbeans:corbeans-spring-boot-starter:$corbeans_version")
    // Corbeans BNMS starter
    compile ("com.github.manosbatsis.corbeans:corbeans-spring-boot-bnms-starter:$corbeans_version")
    // Optional: control the BNMS dependency versions
    	compile "com.r3.businessnetworks:membership-service:$corda_solutions_version"
    	compile "com.r3.businessnetworks:membership-service-contracts-and-states:$corda_solutions_version"
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

### Endpoints

Starting with version 0.25, you need to explicitly add controllers 
to expose BNMS operations for BNO and members via RESTful services.

#### Simple Node Controllers

Implement minimal controller subclasses of `CorbeansBmnsBnoController` 
and/or `CorbeansBmnsMemberController` like bellow:

```kotlin
@RestController
class MyBmnsBnoController: CorbeansBmnsBnoController()


@RestController
class MyBmnsMemberController: CorbeansBmnsMemberController()
``` 

This will expose the folowing endpoints:

Method | Path                                    | Description
------ | --------------------------------------- | -------------------
PUT    | /api/bnms/bno/memberships               | Activate a pending membership
DELETE | /api/bnms/bno/memberships               | Suspend an active membership
GET    | /api/bnms/member/memberships            | Get a memberships list from a BNO. URL params: `bno` (string) the BNO party name, `filterOutMissingFromNetworkMap` (boolean) whether to filter out anyone missing from the Network Map, default is true, `forceRefresh` (boolean) whether to force a refresh, default value is false
POST   | /api/bnms/member/memberships            | Request the BNO to kick-off the on-boarding procedure
PUT    | /api/bnms/member/memberships            | Propose a change to the membership metadata.


This is suitable for a single node per Spring Boot app. To support multiple nodes on the same endpoints 
e.g. by inspecting headers, cookies or whatnot, you can override `getNodeName()`. Alternatively, 
see path fragment based controllers bellow.

#### Path Fragment Based Controllers

Implement minimal controller subclasses of `CorbeansBmnsBnoPathFragmentController` 
and/or `CorbeansBmnsMemberPathFragmentController` like bellow:

```kotlin
@RestController
class MyBmnsBnoController: CorbeansBmnsBnoPathFragmentController()


@RestController
class MyBmnsMemberController: CorbeansBmnsMemberPathFragmentController()
``` 

This will expose the folowing endpoints:

Method | Path                                     | Description
------ | ---------------------------------------- | -------------------
PUT    | /api/bnms/bnos/{nodeName}/memberships    | Activate a pending membership
DELETE | /api/bnms/bnos/{nodeName}/memberships    | Suspend an active membership
GET    | /api/bnms/members/{nodeName}/memberships | Get a memberships list from a BNO. URL params: `bno` (string) the BNO party name, `filterOutMissingFromNetworkMap` (boolean) whether to filter out anyone missing from the Network Map, default is true, `forceRefresh` (boolean) whether to force a refresh, default value is false
POST   | /api/bnms/members/{nodeName}/memberships | Request the BNO to kick-off the on-boarding procedure
PUT    | /api/bnms/members/{nodeName}/memberships | Propose a change to the membership metadata.

Where `nodeName` path fragment is one of: 

- the node name (e.g. `partyA` for configuration properties that read `corbeans.nodes.partyA.*`) 
- the node identity organization name
- the node identity X500 name

### Custom Controllers

Implement a controller by subclassing `CorbeansBmnsBnoBaseController` or `CorbeansBmnsMemberBaseController` 
depending on your requirements.  
