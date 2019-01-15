---
layout: docs
title: "Actuator API"
---

## Actuator API

Corbeans registers some [Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/production-ready-endpoints.html) 
add-ons to expose Corda-related information about known nodes. 

> The [project template](project-template.html)'s Actuator endpoints are __enabled and exposed__ by default.

### Info Contributor

The `CordaInfoContributor` component enriches both HTTP and JMX versions of Actuator's __info__ endpoint with a 
`corda` section:

```js
{
  // Other info...
  "corda" : {
    // Corda-related information, see next section for structure
  }
  // More info...
}
```

The contributor can be disabled by setting the `corbeans.actuator.info.disable` property to `true`.   

### Corda Endpoint

The `CordaInfoEndpoint` component extends Actuator by registering a custom __corda__ endpooint for HTTP and JMX.
By default the endpoint provides all Corda-related information. An additional selector, i.e. path fragment matching 
 a node name per corbeans config, will narrow the response down to the specific node.  


```js
{
    "nodes": {
        "partyA": {
            "platformVersion": 4,
            "peerNames": [
                "O=partyA, L=Athens, C=GR",
                "O=Notary Service, L=Zurich, C=CH",
                "O=partyB, L=Athens, C=GR"
            ],
            "peers": ["Notary Service", "partyB"],
            "identity": "O=partyA, L=Athens, C=GR",
            "notaries": ["O=Notary Service, L=Zurich, C=CH"],
            "flows": [
                "net.corda.core.flows.ContractUpgradeFlow$Authorise",
                "net.corda.core.flows.ContractUpgradeFlow$Deauthorise",
                "net.corda.core.flows.ContractUpgradeFlow$Initiate"
            ],
            "addresses": ["localhost:10004"]
        },
        "partyB": {
            "platformVersion": 4,
            "peerNames": [
                "O=partyB, L=Athens, C=GR",
                "O=partyA, L=Athens, C=GR",
                "O=Notary Service, L=Zurich, C=CH"
            ],
            "peers": ["partyA", "Notary Service"],
            "identity": "O=partyB, L=Athens, C=GR",
            "notaries": ["O=Notary Service, L=Zurich, C=CH"],
            "flows": [
                "net.corda.core.flows.ContractUpgradeFlow$Authorise",
                "net.corda.core.flows.ContractUpgradeFlow$Deauthorise",
                "net.corda.core.flows.ContractUpgradeFlow$Initiate"
            ],
            "addresses": ["localhost:10008"]
        }
        // More nodes...
    }
}
```

The endpoint can be managed as any other Actuator endpoint using `corda` as the endpoint identifier.   