---
layout: docs
title: "Build Howto"
---

# Build Howto

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Cloning the Repo](#cloning-the-repo)
- [Building Corbeans](#building-corbeans)
- [Testing the Webserver](#testing-the-webserver)
- [Building the Website](#building-the-website)

<!-- /TOC -->

The sections bellow are meant for contributors or developers that want to build corbeans sourcecode locally.
You don't need to do this if you only want to use corbeans in your project.

Corbeans requires a recent JDK8.

## Cloning the Repo

```bash
git clone https://github.com/manosbatsis/corbeans.git
```

If you intent to contribute by submitting a PR, it is better to fork the project on Github and clone that instead:

```bash
git clone https://github.com/MYUSENAME/corbeans.git
```

## Building Corbeans

Navigate to the project root folder:

```bash
cd corbeans
```

To build:

```bash
./gradlew build
```

To run a clean build:

```bash
./gradlew clean build
```

To run integration tests:

```bash
./gradlew integrationTest
```

## Testing the Webserver

Besides automatically running integration tests as shown above, you can also manually run the webserver
using an external configuration file (see [Build Howto](build-howto.html)), assuming you have already started the  
corresponding nodes manually:

```bash
./gradlew corbeans-corda-webserver:bootRun --args="--spring.config.location=/path/to/application.properties"
```


## Building the Website

The corbeans documentation website is build using [sbt](https://www.scala-sbt.org/),
[sbt-microsites](https://47deg.github.io/sbt-microsites/) and [jekyll](https://jekyllrb.com/). The site files are in `/docs`.

For prerequisites, see [https://47deg.github.io/sbt-microsites/docs/](https://47deg.github.io/sbt-microsites/docs/).

To build the site, sun sbt:

```bash
sbt
```

While in the sbt insteractive session, run `makeMicrosite`:


```bash
sbt:corbeans> makeMicrosite
```

To preview the site locally, navigate to the generated site directory:

```bash
cd docs/target/site/
```

Start jekyll:

```bash
jekyll serve
```

Navigate to [http://127.0.0.1:4000/corbeans/](http://127.0.0.1:4000/corbeans/) to browse the generated site.

To publish the website on Github pages you need to a) be a contributor and b) have a Github API token set as your `GITHUB_TOKEN`
environment variable:

```bash
sbt:corbeans> publishMicrosite
```
