---
layout: docs
title: "Frequently Asked Questions"
---

## Corda Enterprise

To use Corbeans, [Partiture](https://manosbatsis.github.io/partiture/) 
or [Vaultaire](https://manosbatsis.github.io/vaultaire/) with Corda Enterprise, 
you'll have to instruct your build to replace `net.corda` dependencies with `com.r3.corda`:

```groovy

allprojects {
    //...
    configurations {
        all {
            //...
            resolutionStrategy {
                // ...
                eachDependency { DependencyResolveDetails details ->
                    // Exclude from substitutions as appropriate
                    def exclusions = ['corda-finance-contracts']
                    // Substitute the rest, assumes `ext.corda_release_group` and `ext.corda_release_version` are set
                    if (details.requested.group ==  "net.corda" && !exclusions.contains(details.requested.name)) {
                        // Force Corda Enterprise
                        details.useTarget  group:  corda_release_group, name: details.requested.name, version: corda_release_version
                    }
                }
            }
        }
    }
}
```

> __Note__: The above assumes `ext.corda_release_group` and `ext.corda_release_version` are already set