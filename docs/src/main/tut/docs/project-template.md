---
layout: docs
title: "Project Template"
---

# Project Template


Creating a complete Spring Boot/Corda project from scratch can be a time consuming task.   
The [Corbeans Yo! Cordap](https://github.com/manosbatsis/corbeans-yo-cordapp) project templace can help you bootstrap a fully testable 
application in minutes. The project has state/contract, flow and Spring server modules, 
and even uses [Partiture](https://manosbatsis.github.io/partiture/) for flow composition.



<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Quick HowTo](#quick-howto)
- [Project Modules](#project-modules) 
- [Customisation](#customisation)
	- [Pre-existing Cordapp](#pre-existing-cordapp)
	- [Application Properties](#application-properties)
	- [Custom Package](#custom-package)
	- [Multiple Webservers](#multiple-webservers)

<!-- /TOC -->

## Quick HowTo

	> For Windows, use `gradlew.bat` instead of `./gradlew`

1. Start by cloning the `corbeans-yo-cordapp` template

```bash
git clone https://github.com/manosbatsis/corbeans-yo-cordapp.git
```

2. Navigate to the project directory

```bash
cd corbeans-yo-cordapp
```

3. Build the project and run unit/integration tests

```bash
./gradlew clean build install integrationTest
```

4. Deploy Corda nodes

```bash
./gradlew deployNodes
```

4. Run nodes and webserver

Linux/Unix:

```bash
cordapp-workflow/build/nodes/runnodes
```
Windows:

```bash
call cordapp/build/nodes/runnodes.bat
```

5. Browse the API

(Party A node) 

http://localhost:10007/swagger-ui.html

## Project Modules

- **cordapp-contract**: States , contracts and tests with MockServices.
- **cordapp-workflow**: Flows and tests with MockNetwork.
- **server**: Spring Boot app with a number of approaches to [integration testing](starter-test.html).

## Customisation

### Pre-existing Cordapp

If you need to integrate a pre-existing cordapp you have two options. Either:

- Move your existing contract and workflow modules to the  cordapp-contract and cordapp-workflow modules within the project, or
- Remove the cordapp-contract and cordapp-workflow modules and update the corresponding dependencies in the server module so that it points to your cordapp. 

### Application Properties

You can configure nodes, logging and other options for either runtime or testing by editing
__server/src/main/resources/application.properties__  
or __server/src/test/resources/application.properties__ respectively.

### Custom Package

If you refactor from `mypackage` to your actual base package, make sure to update main and test sources 
throughout project modules, along with the `corbeans.cordapPackages` property in both __application.properties__ 
files in the __server__ module.

### Multiple Webservers

By default `runnodes` will only create a single webserver instance.
If a webserver per node is desired, uncomment "PartyB" node's `webPort` and `webserverJar`
in __cordapp-workflow/build.gradle__'s `Cordform` task.  
