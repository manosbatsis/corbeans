---
layout: docs
title: "Web API"
---

## Web API

Corbeans provides a baseline node API implementation. Bellow are some ways to easily 
expose that as RESTful services.

### Single Node Controller

Implement a minimal controller subclass of `CordaSingleNodeController` like 

```kotlin
@RestController
class NodeController : CordaSingleNodeController()
``` 

This will expose the folowing endpoints:

Method | Path                                    | Description
------ | --------------------------------------- | -------------------
GET    | /node/serverTime            | Return tbe node time in UTC
GET    | /node/whoami                | Returns the Node identity's name
GET    | /node/me                    | Returns the Node identity's x500Principal name
GET    | /node/peers                 | Returns a list of the node's network peers
GET    | /node/peersnames            | Returns a list of the node's network peer names
POST   | /node/attachment            | Saves the given file(s) as a vault attachment and returns a receipt
GET    | /node/attachment/{id}       | Returns the attachment matching the given ID
GET    | /node/attachment/{id}/{path}| Returns a file from within the attachment matching the given ID
GET    | /node/addresses             | Returns a list of node addresses
GET    | /node/notaries              | Returns a list of notaries in node's network
GET    | /node/states                | Returns a list of states
GET    | /node/flows                 | Returns a list of flow classnames


### Multi-node Controller

Implement a minimal controller subclass of `CordaNodesController` like 

```kotlin
@RestController
class NodesController : CordaNodesController()
``` 

This will expose the folowing endpoints:

Method | Path                                    | Description
------ | --------------------------------------- | -------------------
GET    | /nodes/{nodeName}/serverTime            | Return tbe node time in UTC
GET    | /nodes/{nodeName}/whoami                | Returns the Node identity's name
GET    | /nodes/{nodeName}/me                    | Returns the Node identity's x500Principal name
GET    | /nodes/{nodeName}/peers                 | Returns a list of the node's network peers
GET    | /nodes/{nodeName}/peersnames            | Returns a list of the node's network peer names
POST   | /nodes/{nodeName}/attachment            | Saves the given file(s) as a vault attachment and returns a receipt
GET    | /nodes/{nodeName}/attachment/{id}       | Returns the attachment matching the given ID
GET    | /nodes/{nodeName}/attachment/{id}/{path}| Returns a file from within the attachment matching the given ID
GET    | /nodes/{nodeName}/addresses             | Returns a list of node addresses
GET    | /nodes/{nodeName}/notaries              | Returns a list of notaries in node's network
GET    | /nodes/{nodeName}/states                | Returns a list of states
GET    | /nodes/{nodeName}/flows                 | Returns a list of flow classnames

Where `nodeName` is either the node name (e.g. `partyA` for configuration properties that read `corbeans.nodes.partyA.*`) 
or the node identity organization name.


### Custom Controller

Implement a controller by subclassing `CordaSingleNodeController` (possiobly by overriding `getNodeName()`), 
`CordaNodesController` or `CorbeansBaseController` depending on your requirements.  
