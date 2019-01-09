---
layout: docs
title: "Changelog"
---

# Changelog

The following sections describe major changes per version 
and can be helpful with version upgrades.

## 0.19

- Initial changelog
- Added Corda 4.0 as minimum required version  
- Added `CordaNetworkService` as a convenient, autowirable entry point to API
- Added `StateService` helpers 
- Removed `WithDriverNodesIT.getCordappPackages` 
and `WithImplicitNetworkIT.getCordappPackages` methods 
in favor of using `corbeans.cordapPackages` in `application.properties`
- Added `CorbeansSpringExtension` for implicit network in integration tests as an alternative to subclassing `WithImplicitNetworkIT`
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
	import com.github.manosbatsis.corbeans.spring.boot.corda.config.NodeParams
	import com.github.manosbatsis.corbeans.spring.boot.corda.rpc.NodeRpcConnection
	import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeService
	import com.github.manosbatsis.corbeans.spring.boot.corda.service.CordaNodeServiceImpl
	```

