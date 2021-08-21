---
layout: docs
title: "Changelog"
---

# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.


## 0.52-0.56

- Updated dependencies.

## 0.51

- Updated Corda Testacles to 0.13.
- All instances of `cordapPackages` have been renamed to 
`cordappPackages`, including code and properties.

## 0.50

- Updated dependencies, upgraded to Corda to 4.6 

## 0.49

- Updated vaultaire, corda-rpc-poolboy 

- Support custom Cordapp Config during test execution using 
prperty files. 

## 0.47

- Bumped Vaultaire to v0.30

## 0.46

- Bumped Vaultaire to v0.29

## 0.45

- Improved support for Corda Accounts

## 0.43-44

- Corbeans no longer requires a fixed number of nodes.
Nodes (and their RPC connection config) are now completely dynamic, 
so no `CordaNodeService` beans will be registered anymore. 
Instead, you must use `CordaNetworkService.getNodeService(nodeName)` 
to obtain a node service for the target node.
- All services now use RPC connection pooling based on  
(Corda RPC PoolBoy)[https://github.com/manosbatsis/corda-rpc-poolboy] 
under the hood.
- You should no longer use a `CordaRPCOps` directly. Instead, 
use `CordaNetworkService.withNodeRpcConnection` or 
`CordaNodeService.withNodeRpcConnection` to enclose blocks 
that call flows etc.
- Removed the `corbeans.nodes.default.primaryServiceType` property. 
If you want to customize the `CordaNodeService` implementastion 
used, you will need to override/use your own `CordaNetworkService`
bean, or use a regular node service as a Kotlin delegate.
- Removed `corbeans.nodes.default.bnmsServiceType`. You must now 
use `corbeans.bnmsServiceType` to specify a `CordaBnmsService`  
implementation class. 

## 0.42

- Fixed generated POMs

## 0.41

- `CorbeansMockNodeParametersConfig` and thus `CorbeansMockNetworkFlowTest`
will now ignore `default` and `cordform` node configurations when creating a MockNetwork
- Corbeans will no longer add an `ObjectMapper` from corda-jackson
- Added workaround/fix for a Corda `LinkageError` when it's decorated `AttachmentURLStreamHandlerFactory` was applied
- Bumped Corda to 4.4, Vaultaire to 0.25

## 0.40

- Bumped Vaultaire to 0.20

## 0.39

- Fixed `com.github.manosbatsis.corbeans.test.integration.CorbeansSpringExtension` 
trying to launch multiple Corda networks when using inner test classes

## 0.38

- Refactored to `com.github.manosbatsis.vaultaire.rpc.NodeRpcConnection` interface

## 0.37

- Bumbed vaultaire version

## 0.37

- Build cleanup

## 0.36

- Added `disableGracefulReconnect` configuration property, default is `false`

## 0.35

- Switched to Corda 4.3's [automatic RPC reconnection](https://docs.corda.net/clientrpc.html?highlight=rpc#enabling-automatic-reconnection)

## 0.34

- Updated to and requiring Corda 4.3, at least for running tests. 
If you are on Corda 4.1, it is recommended you use Corbeans 0.33.
- Extracted common `CordaRpcService` base class for node and BNMS services to extend from.
- Added alternative method signatures to `CordaBnmsService` and applied the 
membership metadata param regularly. 

## 0.30-33

- Misc improvements and fixes.

## 0.29

- Fixed [integration tests for Corda Enterprise 4.2](https://github.com/manosbatsis/corbeans/issues/24)

## 0.28

- `NodeDriverHelper` now explicitly stops and closes nodes
- Added `refreshNetworkMapCache` endpoint and service method

## 0.27

- Refactored and moved `StateService` to the [Vaultaire](https://manosbatsis.github.io/vaultaire) project

## 0.26

- Improved `UniqueIdentifierConverter` to properly handle externalIDs that contain an underscore.
- Return a 404 `ResponseStatusException` when an attachment (hash) is not found
- Upgraded to Corda 4.1 and Gradle (wrapper) 4.10.2.

## 0.25

- From now on Corbeans BNMS controllers will have to be explicitly added if desired, see [BNMS Starter](starter-bnms.html).

## 0.24

- From now on a Corbeans "Node" controller will have to be explicitly added if desired, see [Web API](web-api.html)
- `corbeans.nodes.xxx.testPartyName` can be used to set the Node identity for integration tests
- Both X500 and organization names of a Node Party (`Party`) can now be used as the `nodeName` path fragment
- Updated Spring, Spring Boot dependencies (5.1.8.RELEASE, 2.1.6.RELEASE)

## 0.23

- Added BNMS starter module
- Added Spring converter for `UniqueIdentifier`
- Cleaned up nodes/peers endpoints, they now return `List<PartyNameModel>`
- Added `corbeans.objectmapper.enableRpc` property 

## 0.22

- Deprecated `WithImplicitNetworkIT` in favour of `CorbeansSpringExtension`
- Updated Corda platform to version 4.0

## 0.21

- Updated Corda platform and plugins to 4.0-RC07 and 4.0.40 respectively.

## 0.20

- Improved exception handling in `NodeRpcConnection` attempts
- Added support for `ClientRpcSslOptions` configuration per node in `application.properties` 

## 0.19

- Initial changelog
- Added Corda 4.0 as minimum required version  
- Added [template project](project-template.html)
- Added `corbeans.nodes.default.*` properties for global node defaults
- Added new config properties per node and `CordaRPCClientConfiguration` updates in Corda 4.0
- Added `CordaNetworkService` as a convenient, autowirable entry point to API
- Added `StateService` helpers 
- Removed `WithDriverNodesIT.getCordappPackages` 
and `WithImplicitNetworkIT.getCordappPackages` methods 
in favor of using `corbeans.cordapPackages` configuration in __application.properties__
- Added `CorbeansSpringExtension` for implicit network during integration tests as an alternative to subclassing `WithImplicitNetworkIT`
- Fixed `WithImplicitNetworkIT` issue with test hanging in some cases
- Moved REST controller endpoints from `/node` and `/nodes/{nodeName}` to 
`/api/node` and `/api/nodes/{nodeName}` respectively
- Refactored packages, for example 
	```kotlin
	import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeParams
	import com.github.manosbatsis.corbeans.spring.boot.corda.util.NodeRpcConnection
	import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeService
	import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
	```
	is now
	```kotlin
	import com.github.manosbatsis.corbeans.corda.common.NodeParams
	import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.AbstractNodeRpcConnection
	import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
	import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeServiceImpl
	```
- Refactored return type of `CordaNodeService.peers` and `CordaNodeService.peerNames` from 
`Map<String, List<String>>` to simply `List<String>`
- Added basic Spring boot Actuator components: an __info__ endpoint contributor and a custom 
__corda__ HTTP/JMX endpoint 
- Added endpoints for saving attachments and browsing attachment archive contents
- Added Spring converter for `CordaX500Name`
- Added ObjectMapper auto-configuration with RPC support
- CorbeansSpringExtension for JUnit5 now starts nodes and the container in the correct order

