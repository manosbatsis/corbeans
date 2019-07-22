---
layout: docs
title: "Web API"
---

## Web API

Starting with version 0.24, you need to explicitly add a controller 
to expose node operations via RESTful services.

### Simple Node Controller

Implement a minimal controller subclass of `CordaNodeController` like 

```kotlin
@RestController
class NodeController : CordaNodeController()
``` 

This will expose the folowing endpoints:

Method | Path                                    | Description
------ | --------------------------------------- | -------------------
GET    | /api/node/serverTime            | Return tbe node time in UTC
GET    | /api/node/whoami                | Returns the Node identity's name
GET    | /api/node/me                    | Returns the Node identity's x500Principal name
GET    | /api/node/peers                 | Returns a list of the node's network peers
GET    | /api/node/peersnames            | Returns a list of the node's network peer names
POST   | /api/node/attachment            | Saves the given file(s) as a vault attachment and returns a receipt
GET    | /api/node/attachment/{id}       | Returns the attachment matching the given ID
GET    | /api/node/attachment/{id}/{path}| Returns a file from within the attachment matching the given ID
GET    | /api/node/addresses             | Returns a list of node addresses
GET    | /api/node/notaries              | Returns a list of notaries in node's network
GET    | /api/node/states                | Returns a list of states
GET    | /api/node/flows                 | Returns a list of flow classnames


> This is suitable for a single node setup. To support multiple nodes on the same endpoints 
e.g. by inspecting headers, cookies or whatnot, you can override `getNodeName()`. Alternatively, 
see `CordaPathFragmentNodeController` bellow.

### Path Fragment Based Controller

Implement a minimal controller subclass of `CordaPathFragmentNodeController` like 

```kotlin
@RestController
class MyCordaNodeController: CordaPathFragmentNodeController()
``` 

This will expose the folowing endpoints:

Method | Path                                    | Description
------ | --------------------------------------- | -------------------
GET    | /api/nodes/{nodeName}/serverTime            | Return tbe node time in UTC
GET    | /api/nodes/{nodeName}/whoami                | Returns the Node identity's name
GET    | /api/nodes/{nodeName}/me                    | Returns the Node identity's x500Principal name
GET    | /api/nodes/{nodeName}/peers                 | Returns a list of the node's network peers
GET    | /api/nodes/{nodeName}/peersnames            | Returns a list of the node's network peer names
POST   | /api/nodes/{nodeName}/attachment            | Saves the given file(s) as a vault attachment and returns a receipt
GET    | /api/nodes/{nodeName}/attachment/{id}       | Returns the attachment matching the given ID
GET    | /api/nodes/{nodeName}/attachment/{id}/{path}| Returns a file from within the attachment matching the given ID
GET    | /api/nodes/{nodeName}/addresses             | Returns a list of node addresses
GET    | /api/nodes/{nodeName}/notaries              | Returns a list of notaries in node's network
GET    | /api/nodes/{nodeName}/states                | Returns a list of states
GET    | /api/nodes/{nodeName}/flows                 | Returns a list of flow classnames

Where `nodeName` path fragment is one of: 

- the node name (e.g. `partyA` for configuration properties that read `corbeans.nodes.partyA.*`) 
- the node identity organization name
- the node identity X500 name


### Custom Controller

Implement a controller by subclassing `CordaNodeController`, 
`CordaPathFragmentNodeController` or `CorbeansBaseController` depending on your requirements.  
