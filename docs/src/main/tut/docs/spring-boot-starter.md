---
layout: docs
title: Spring-Boot Starter
---

# Spring-Boot Starter

Corda provides a client RPC library that allows you to interact with a running node. The starting point for the client 
library is the `CordaRPCClient` class, which provides a `start` method that returns a `CordaRPCConnection`.  
A `CordaRPCConnection` allows you to access an implementation of the `CordaRPCOps` interface with proxy in Kotlin or 
getProxy() in Java. More detail on this functionality is provided in the [RPC docs](https://docs.corda.net/clientrpc.html).

The Spring-Boot starter automatically creates and configures REST Controller, Service components that wrap such 
a `CordaRPCOps` proxy for each one of your Corda nodes. All you need is the starter dependency in your project and the 
node connection parameters in `application.properties`. The following sections will quickly quide you through 
installing and configuring the starter according to your your needs:

- [Installation](starter/installation.html)
- [Configuration](starter/configuration.html)
- [Autowiring](starter/autowiring.html)

Next: [Installation](starter/installation.html)