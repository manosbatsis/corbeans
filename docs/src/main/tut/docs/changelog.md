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
- Removed `WithDriverNodesIT.getCordappPackages` 
and `WithImplicitNetworkIT.getCordappPackages` methods 
in favor of using `corbeans.cordapPackages` in `application.properties`
- Added `CorbeansSpringExtension` for implicit network in integration tests