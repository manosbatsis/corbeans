---
layout: docs
title: "Frequently Asked Questions"
---
# Frequently Asked Questions

## License

Corbeans is distributed under the GNU __Lesser__ General Public License or LGPL. This is the same 
license adopted by Corda dependencies like Hibernate. It allows Corbeans to be used as a library 
with no effect to your project.

## Corda Enterprise

To use Corbeans, [Partiture](https://manosbatsis.github.io/partiture/) 
or [Vaultaire](https://manosbatsis.github.io/vaultaire/) with Corda Enterprise, 
you will have to update your build to [use the CE release](https://docs.corda.r3.com/app-upgrade-notes-enterprise.html).

After switching to the appropriate corda_release_group and corda_release_version in your ext section, you can instruct your build to substitute transitive Corda OS dependencies with their CE equivalents: 

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

> __Note__: The above assumes ext.corda_release_group and ext.corda_release_version are already set, e.g. to com.r3.corda and 4.2 respectively.