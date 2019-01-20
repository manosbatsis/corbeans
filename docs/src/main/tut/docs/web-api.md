---
layout: docs
title: "Web API"
---

## Web API

Corbeans registers default controller endpoints, see bellow. 

If you are using the template, you can browse the API documentation in 
Swagger UI

### Endpoints

The `CordaNodesController` already mentioned in ["Getting Started"](getting-started.html)  
adds a set of default service endpoints:

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

> **Note:** When corbeans parses node.conf as the configuration (i.e. when using `CordForm` and/or `runnodes`),
the corresponding base path for the default node endpoints is simply `node` instead of `nodes/{nodeName}`. Internally, 
`cordform` is the actual value of `nodeName` for "default" endpoints, node services, etc.
