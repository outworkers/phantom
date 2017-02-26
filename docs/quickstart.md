[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

<a id="integrating-phantom">Integrating phantom in your project</a>
===================================================================
<a href="#table-of-contents">back to top</a>

There are no additional resolvers required for any version of phantom newer than 2.0.0. All Outworkers libraries are open source,
licensed via Apache V2. As of version 2.2.1, phantom has no external transitive dependencies other than shapeless
and the Java driver.

#### For must things, all you need is a dependency on the phantom-dsl module.

For most things, all you need is the main ```phantom-dsl``` module. This will bring in the default module with all the query generation ability, as well as `phantom-connectors` and database objects that help you manage your entire database layer on the fly. All other modules implement enhanced integration with other tools, but you don't need them to get started.
This module only depends on the `datastax-java-driver` and the `shapeless-library`.

```scala
libraryDependencies ++= Seq(
  "com.outworkers"  %% "phantom-dsl" % phantomVersion
)
```

#### The full list of available modules

The full list of available modules is:

```scala
libraryDependencies ++= Seq(
  "com.outworkers"   %% "phantom-connectors" % phantomVersion,
  "com.outworkers"   %% "phantom-dsl" % phantomVersion,
  "com.outworkers"   %% "phantom-example" % phantomVersion,
  "com.outworkers"   %% "phantom-finagle" % phantomVersion,
  "com.outworkers"   %% "phantom-jdk8" % phantomVersion,
  "com.outworkers"   %% "phantom-thrift" % phantomVersion,
  "com.outworkers"   %% "phantom-streams" % phantomVersion,
  "com.outworkers"   %% "phantom-sbt" % phantomVersion
)
```
If you include `phantom-finagle` or `phantom-thrift`, make sure to add the following resolvers:

```scala
resolvers += "twitter-repo" at "http://maven.twttr.com"
```

#### Using phantom-sbt to test requires custom resolvers

In your `plugins.sbt`, you will also need this if you plan to use `phantom-sbt`:

```scala

def baseResolverPattern = {
  Patterns(Resolver.mavenStyleBasePattern, Resolver.mavenStyleBasePattern, true)
}

resolvers ++= Seq(
  Resolver.url("Maven Ivy Outworkers", url(Resolver.DefaultMavenRepositoryRoot))(baseResolverPattern)
)

addSbtPlugin("com.outworkers" %% "phantom-sbt" % phantomVersion)

```


#### Using phantom with the Spray framework

Spray users will probably be affected by a conflict in shapeless versions. To fix the conflict, add the following dependency to your build, which will allow you to use phantom and Spray together without any issues.

```scala
libraryDependencies ++= Seq(
  "io.spray" %% "spray-routing-shapeless2" % SprayVersion
)
```