---
layout: docs
title: "Components API"
---

## Component API

This document provides an overview of the API used within Spring components.

### Autowiring Services

Corbeans now supports any dynamic number of nodes. As a consequence, 
node services are not pre-registered Spring beans anymore. 
Instead, the network service creates node services as short-lived 
helpers (think request context etc.). Kotlin example:

```kotlin
    
    // Autowire a network service, used to access node services
    @Autowired
    lateinit var networkService: CordaNetworkService

    fun doSomething(){
        
        // Get a node service
        val nodeService = networkService
            .getNodeService("partyA")    


    }
```

or Java

```java

    // Autowire a network service, used to access node services
    @Autowired
    private CordaNetworkService networkService;
    
    public void doSomething(){
    
        // Get a node service
        CordaNodeService nodeService = networkService
            .getNodeService("partyA") 
    }
```

### API Overview 

> Network and state services are only available from version .19 and up.

#### Network Service

Network service is a root component you can optionally use to 
create node service instances:
 
```kotlin
// (Optional) Use CordaNetworkService to access node services
val nodeService =  networkService.getNodeService("optional name")
```

#### Node Service

Node services are useful in two main ways:

1. They provide an extensive API related to identities, parties, attachments, 
flows and so on.
	```kotlin
	// Find the party matching the name
	val machingParties = nodeService.partiesFromName("party A")
	```
2. They can create `StateService` helpers for the desired 
`ContractState` type. 
	```kotlin
	val stateService = nodeService.createStateService(MyContractState::class.java)
	```
	
#### State Service

Node services can help you query or track states of a 
certain `ContractState` type:

```kotlin
// Get a state service
val myStateService = networkService.getNodeService("partyA")
		.createStateService(MyState::class.java)
// Query states 
val myStates = myStateService.queryBy(criteria, pageSpec, sort)
// Observe and count Yo! updates
val myUpdates = mutableListOf<MyState>()
val myStateVaultObservable = myStateService.trackBy().updates   
myStateVaultObservable.subscribe { update ->
	update.produced.forEach { (state) ->
		myUpdates.add(state.data)
	}
}
```
