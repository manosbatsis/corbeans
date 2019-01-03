---
layout: docs
title: "Project Template"
---

# Project Template


The Corbeans Yo! Cordap is a project sample and templace that can help you setup a proper project in minutes.

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Quick HowTo](#quick-howto)
- [Project Modules](#project-modules)
- [Customisation](#customisation)
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
cordapp/build/nodes/runnodes
```
Windows:

```bash
call cordapp/build/nodes/runnodes.bat
```

## Project Modules

```bash
corbeans-yo-cordapp
├── cordapp            # Aggregate cordapp
├── cordapp-contract   # States and contracts cordapp and tests
├── cordapp-workflow   # Flows cordapp and tests
└── server             # Spring Boot server app and tests
```


## Customisation

### Application Properties

You can configure nodes, logging and other options for either runtime or testing by editing
__server/src/main/resources/application.properties__  
or __server/src/test/resources/application.properties__ respectively.

### Custom Package

If you refactor from `mypackage` to your actual package, make sure to make the appropriate
changes in both __application.properties__ files, including

### Multiple Webservers

By default `runnodes` will only create a single webserver instance.
If a webserver per node is desired, uncomment "PartyB" node's `webPort` and `webserverJar`
in __cordapp/build.gradle__'s `Cordform` task.  
