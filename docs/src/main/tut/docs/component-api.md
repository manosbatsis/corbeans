---
layout: docs
title: "Components API"
---

## Component API

This document provides an overview of the API used within Spring components.

### Autowiring Services

Service beans registered by corbeans may be autowired as any other component, for example in Kotlin:

```kotlin
    
    // Autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService
    // Autowire all created node services directly, mapped by name
    @Autowired
    lateinit var services: Map<String, CordaNodeService>
    // Autowire a node-specific service
    @Autowired
    @Qualifier("partyANodeService")
    lateinit var service: CordaNodeService
    // You can also specify a custom type explicitly
    // for nodes configured using the  `primaryServiceType`
    // application property (see following section)
    @Autowired
    @Qualifier("partyBNodeService")
    lateinit var customCervice: SampleCustomCordaNodeServiceImpl
```

or Java

```java

    // Autowire a network service, used to access node services
    @Autowired
    private CordaNetworkService networkService;
    // Autowire all created node services directly, mapped by name
    @Autowired
    private Map<String, CordaNodeService> services;
    // Autowire a node-specific service
    @Autowired
    @Qualifier("partyANodeService")
    private CordaNodeService service;
    // You can also specify a custom type explicitly
    // for nodes configured using the  `primaryServiceType`
    // application property (see following section)
    @Autowired
    private SampleCustomCordaNodeServiceImpl customCervice;
```

### API Overview 

> Network and state services are only available from version .19 and up.

#### Network Service

Network service is just a root component you can optionally use to 
obtain nodes service instances:
 
```kotlin
// (Optional) Use CordaNetworkService to access node services
val nodeService =  networkService.getNodeService("optional name")
```

#### Node Service

Node services are used mostly for three things:

1. Getting the RPC proxy (`CordaRPCOps`) for the service's
	```kotlin
	val rpcOps = nodeService.proxy()
	```
2. Utility methods to obtain information related to identities, parties, attachments, flows and so on.
	```kotlin
	// Find the party matching the name
	val machingParties = nodeService.partiesFromName("party A")
	```
3. Getting a `StateService` helper for the desired `ContractState` type - see bellow. 
	```kotlin
	val stateService = nodeService.createStateService(MyContractState::class.java)
	```
	
#### State Service

Node services are mostly used to query or track states of a certain type:

```kotlin
// Get a state service
val myStateService = networkService.getNodeService("partyA")
		.createStateService(MyState::class.java)
// Query states 
val myStates = myStateService.query() // or queryBy...
// Observe and count Yo! updates
val myUpdates = mutableListOf<MyState>()
val myStateVaultObservable = myStateService.track().updates // or trackBy...
myStateVaultObservable.subscribe { update ->
	update.produced.forEach { (state) ->
		myUpdates.add(state.data)
	}
}
```

### Advanced Configuration

You can instruct Corbeans to create and register your custom node service implementations.
The only requirement is that you have to extend `CordaNodeServiceImpl`
(or otherwise implement `CordaNodeService`), for example in Kotlin:

```kotlin
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl
import com.github.manosbatsis.vaultaire.rpc.NodeRpcConnection

class SampleCustomCordaNodeServiceImpl(
        nodeRpcConnection: NodeRpcConnection
) : CordaNodeServiceImpl(nodeRpcConnection) {

    /** dummy method */
    fun dummy(): Boolean = true

}
```

or Java:


```java
import com.github.manosbatsis.corbeans.spring.boot.corda.CordaNodeServiceImpl;
import com.github.manosbatsis.vaultaire.rpc.NodeRpcConnection;

public class SampleCustomCordaNodeServiceImpl extends CordaNodeServiceImpl {

	public SampleCustomCordaNodeServiceImpl(NodeRpcConnection nodeRpcConnection){
		super(nodeRpcConnection);
	}
    /** dummy method */
    public Boolean dummy(){return true;}
}
```

Then instruct corbeans to use your custom service type via the
`primaryServiceType` property for the desired node:

```properties
# node for PartyB
# Set the node service type for party A
corbeans.nodes.PartyA.primaryServiceType=my.subclass.of.CordaNodeServiceImpl
# Override the default service implementation for all nodes 
# (more specific config above still wins)
corbeans.nodes.default.primaryServiceType=my.subclass.of.CordaNodeServiceImpl
```  
