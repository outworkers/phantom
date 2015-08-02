phantom [![Build Status](https://travis-ci.org/websudos/phantom.svg?branch=develop)](https://travis-ci.org/websudos/phantom) [![Coverage Status](https://coveralls.io/repos/websudos/phantom/badge.svg)](https://coveralls.io/r/websudos/phantom) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom_2.10) [![Download](https://api.bintray.com/packages/websudos/oss-releases/phantom/images/download.svg) ](https://bintray.com/websudos/oss-releases/phantom/_latestVersion)

==============
Reactive type-safe Scala DSL for Cassandra

To stay up-to-date with our latest releases and news, follow us on Twitter: [@websudos](https://twitter.com/websudos).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be completely free and open source, 
but the more adopters our projects have, the more people from our company will actively work to make them better.


![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Websudos Phantom")


Using phantom
=============

### Scala 2.10 and 2.11 releases ###

We publish phantom in 2 formats, stable releases and bleeding edge.

- The stable release is always available on Maven Central and will be indicated by the badge at the top of this readme. The Maven Central badge is pointing at the latest version

- Intermediary releases are available through our managed Bintray repository available at `https://dl.bintray.com/websudos/oss-releases/`. The latest version available on our Bintray repository is indicated by the Bintray badge at the top of this readme.


### Latest versions

Check the badges at the top of this README for the latest version. The badges are automatically updated in realtime, where as this README isn't.

- Latest stable version: 1.8.9 (Maven Central)
- Bleeding edge: 1.8.12 (Websudos OSS releases on Bintray)

You will also be needing the default resolvers for Maven Central and the typesafe releases. Phantom will never rely on any snapshots or be published as a
snapshot version, the bleeding edge is always subject to internal scrutiny before any releases into the wild.

The Apache Cassandra version used for auto-embedding Cassandra during tests is: ```val cassandraVersion = "2.1.0-rc5"```. You will require JDK 7 to use 
Cassandra, otherwise you will get an error when phantom tries to start the embedded database. The recommended JDK is the Oracle variant.


### Version highlights and upcoming features ###

<ul>
    <li><a href="#new-querybuilder">1.8.0: A new QueryBuilder, written from the ground up, in idiomatic Scala</a></li>
    <li><a href="#alter-queries">1.8.0: Added support for type-safe ALTER queries</a></li>
    <li><a href="#advanced-cql-support">1.8.0: Support for advanced CQL options</a></li> 
    <li><a href="#prepared-statements">1.9.0: Type safe prepared statements</a></li>
    <li><a href="#automigration">1.9.0: Automated Schema migrations</li>
    <li><a href="#udts">2.0.0: Type safe user defined types</li>
    <li>
      <a href="#breaking-changes">Breaking changes in DSL and connectors
      <ul>
        <li><a href="#new-imports">A new import structure</a></li>
        <li><a href="#propagating-parse-errors">Propagating parse errors</a></li>
      </ul>
    </a>
    <li><a href="#autocreation">1.9.0: Automated table creations</li>
    <li><a href="#autotruncation">1.9.0: Automated table truncation.</li>
    <li><a href="#performance">1.9.0: Big performance improvements</li>
</ul>


### Breaking API changes in Phantom 1.8.0 and beyond.

The 1.8.0 release constitutes a major re-working of a wide number of internal phantom primitives, including but not limited to a brand new Scala flavoured
QueryBuilder with full support for all CQL 3 features and even some of the more "esoteric" options available in CQL. We went above and beyond to try and
offer a tool that's comprehensive and doesn't miss out on any feature of the protocol, no matter how small.

If you are wondering what happened to 1.7.0, it was never publicly released as testing the new querybuilder entailed serious internal efforts and for such a drastic change
we wanted to do as much as possible to eliminate bugs. Surely there will be some still found, but hopefully very few and with your help they will be very short lived.

Ditching the Java Driver was not a question of code quality in the driver, but rather an opportunity to exploit the more advanced Scala type system features
to introduce behaviour such as preventing duplicate limits on queries using phantom types, to prevent even more invalid queries from compiling, and to switch
 to a fully immutable QueryBuilder that's more in tone with idiomatic Scala, as opposed to the Java-esque mutable alternative already existing the java driver.


<a id="new-imports">A new import structure</a>
================================================

```import com.websudos.phantom.Implicits._``` has now been renamed to ```import com.websudos.phantom.dsl._```. The old import is still there but deprecated.

A natural question you may ask is why we resorted to seemingly unimportant changes, but the goal here was to enforce the new implicit mechanism and use a uniform importing experience across all modules.
So you can have the series of ```import com.websudos.phantom.dsl._, import com.websudos.phantom.thrift._, import com.websudos.phantom.testkit._``` and so on, all identical, all using Scala ```package object``` definitions as intended.

<a id="propagating-parse-errors">Propagating parse errors</a>
=============================================================

Until now, our implementation of Cassandra primitives has been based on the Datastax Java Driver and on an ```Option``` based DSL. This made it hard to deal with parse errors at runtime, specifically in those situations when
the DSL was unable to parse the required type from the Cassandra result or in a simple case where ```null``` was returned for a non-optional column.

The core of the ```Column[Table, Record, ValueType].apply(value: ValueType]``` method which was used to parse rows in a type safe manner was written like this:

```scala

import com.datastax.driver.core.Row

def apply(row: Row):  = optional(row).getOrElse(throw new Exception("Couldn't parse things")

```

This approach discarded the original exception which caused the parser to parse a ```null``` and subsequently a ```None``` was ignored.

With the new type-safe primitive interface that no longer relies on the Datastax Java driver we were also able to move the ```Option``` based parsing mechanism to a ```Try``` mechanism which will now
 log all parse errors un-altered, in the exact same way as are thrown at compile time, using the ```logger``` for the given table.
 
Internally, we are now using something like this: 
 
```scala

   def optional(r: Row): Try[T]
 
   def apply(r: Row): T = optional(r) match {
     case Success(value) => value
     case Failure(ex) => {
       table.logger.error(ex.getMessage)
       throw ex
     }
   }

```

The exception is now logged and propagated as is. We intercept it to provide consistent logging in the same table logger where you would naturally monitor for logs. 


<a id="improving-query-performance">Improving query performance</a>
==================================================================

Play enumerators and Twitter ResultSpools have been removed from the default ```one```, ```get```, ```fetch``` and ```collect``` methods. You will have to
explicitly call ```fetchEnumerator``` and ```fetchSpool``` if you want result throttling through async lazy iterators. This will offer everyone a signifact
performance improvement over query performance. Async iterators needed a lot of expensive "magic" to work properly, but you don't always need to fold over
100k records. That behaviour was implemented both as means of showing off as well as doing all in one loads like the Spark - Cassandra connector performs. E.g
 dumping C* data into HDFS or whatever backup system. A big 60 - 70% gain should be expected.

Phantom connectors now require an ```implicit com.websudos.phantom.connectors.KeySpace``` to be defined. Instead of using a plain string, you just have to
use ```KeySpace.apply``` or simply: ```trait MyConnector extends Connector { implicit val keySpace = KeySpace("your_def") } ```. This change allows us to
replace the existing connector model and vastly improve the number of concurrent cluster connections required to perform operations on various keyspaces.
Insteaed of the 1 per keyspace model, we can now successfully re-use the same session without evening needing to switch as phantom will use the full CQL
reference syntax, e.g ```SELECT FROM keyspace.table``` instead of ```SELECY FROM table```.

A entirely new set of options have been enabled in the type safe DSLs. You can now alter tables, specify advanced compressor behaviour and so forth, all
from within phantom and with the guarantee of auto-completion and type safety.


#### Support for ALTER queries.

This was never possible before in phantom, and now from 1.7.0 onwards we feature full support for using ALTER queries.



<a id="table-of-contents">Table of contents</a>
===============================================

<li><a href="#contributors">Contributing to phantom</a></li>
<li><a href="#using-gitflow">Using GitFlow as a branching model</a></li>
<li><a href="#scala-style-guidelines">Scala style guidelines for contributions</a></li>
<li><a href="#copyright">Copyright</a></li>


<a id="issues-and-questions">Issues and questions</a>
=====================================================
<a href="#table-of-contents">back to top</a>

We love Cassandra to bits and use it in every bit of our stack. phantom makes it super trivial for Scala users to embrace Cassandra.

Cassandra is highly scalable and it is by far the most powerful database technology available, open source or otherwise.

Phantom is built on top of the [Datastax Java Driver](https://github.com/datastax/java-driver), which does most of the heavy lifting. 

If you're completely new to Cassandra, a much better place to start is the [Datastax Introduction to Cassandra](http://www.datastax.com/documentation/getting_started/doc/getting_started/gettingStartedIntro_r.html). An even better introduction is available on [our blog]
(http://blog.websudos.com/category/nosql/cassandra/), where we have a full series of introductory posts to Cassandra with phantom.

We are very happy to help implement missing features in phantom, answer questions about phantom, and occasionally help you out with Cassandra questions! Please use GitHub for any issues or bug reports.

Adopters
========

This is a list of companies that have embraced phantom as part of their technology stack and using it in production environments.

- [CreditSuisse](https://www.credit-suisse.com/global/en/)
- [Pellucid Analytics](http://www.pellucid.com/)
- [Sphonic](http://www.sphonic.com/)
- [websudos](https://www.websudos.com/)
- [Equens](http://www.equens.com/)
- [VictorOps](http://www.victorops.com/)
- [Socrata](http://www.socrata.com)

Roadmap
========

While dates are not fixed, we will use this list to tell you about our plans for the future. If you have great ideas about what could benefit all phantom 
adopters, please get in touch. We are very happy and eager to listen.

- User defined types

We are working closely around the latest features in the Datastax Java driver and Apache Cassandra 2.1 to offer a fully type safe DSL for user defined types.
This feature is well in progress and you can expect to see it live roughly at the same time as the release of the Datastax 2.1 driver, planned for July 2014.

Some of the cool features include automatic schema generation, fully type safe referencing of fields and inner members of UDTs and fully type safe querying.


- Spark integration

Thanks to the recent partnership between Databricks and Datastax, Spark is getting a Cassandra facelift with a Datastax backed integration. We won't be slow to
follow up with a type safe Scala variant of that integration, so you can enjoy the benefits of high power computation with Cassandra as a backup
 storage through the simple high power DSL we've gotten you used to.

- Prepared statements

By popular demand, a feature long overdue in phantom. The main reason is the underlying Java driver and the increased difficulty of guaranteeing type safety
with prepared statements along with a nice DSL to get things done. Not to say it's impossible, this will be released after the new query builder emerges.

- A new QueryBuilder(available as of 1.6.0)

- Zookeeper support(available as of 1.1.0).


<a id="learning-phantom">Tutorials on phantom and Cassandra</a>
======================================================================

For ease of use and far better management of documentation, we have decided to export the `README.md` to a proper
Wiki page, now available [here](https://github.com/websudos/phantom/wiki/). This is a standard Github Wiki in markdown format that will allow us to add documentation at a much much faster pace than before and hopefully vastly improve your experience using phantom.



<a id="contributors">Contributors</a>
=====================================
<a href="#table-of-contents">back to top</a>

Phantom was developed at websudos as an in-house project. All Cassandra integration at Websudos goes through phantom, and nowadays it's safe to say most
Scala/Cassandra users in the world rely on phantom.

* Flavian Alexandru ([@alexflav23](https://github.com/alexflav23)) - maintainer
* Viktor Taranenko ([@viktortnk](https://github.com/viktortnk))
* Benjamin Edwards ([@benjumanji](https://github.com/benjumanji)
* Jens Halm ([@jenshalm](https://github.com/jenshalm))
* Bartosz Jankiewicz ([@bjankie1](https://github.com/bjankie1)
* Eugene Zhulenev ([@ezhulenev](https://github.com/ezhulenev)
* Stephen Samuel ([@sksamuel](https://github.com/sksamuel)
* Tomasz Perek ([@tperek](https://github.com/tperek)
* Evan Chan ([@evanfchan](https://github.com/evanfchan)

<a id="copyright">Copyright</a>
===============================
<a href="#table-of-contents">back to top</a>

Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Copyright 2013 - 2015 websudos.


Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome! Use GitHub for issues and pull requests and we will happily help out in any way we can!

<a id="git-flow">Using GitFlow</a>
==================================

To contribute, simply submit a "Pull request" via GitHub.

We use GitFlow as a branching model and SemVer for versioning.

- When you submit a "Pull request" we require all changes to be squashed.
- We never merge more than one commit at a time. All the n commits on your feature branch must be squashed.
- We won't look at the pull request until Travis CI says the tests pass, make sure tests go well.

<a id="style-guidelines">Scala Style Guidelines</a>
===================================================

In spirit, we follow the [Twitter Scala Style Guidelines](http://twitter.github.io/effectivescala/).
We will reject your pull request if it doesn't meet code standards, but we'll happily give you a hand to get it right.

Some of the things that will make us seriously frown:

- Blocking when you don't have to. It just makes our eyes hurt when we see useless blocking.
- Testing should be thread safe and fully async, use ```ParallelTestExecution``` if you want to show off.
- Writing tests should use the pre-existing tools, they bring in EmbeddedCassandra, Zookeeper and other niceties, allowing us to run multi-datacenter tests.
- Use the common patterns you already see here, we've done a lot of work to make it easy.
- Don't randomly import stuff. We are very big on alphabetized clean imports.
- Tests must pass on both the Oracle and OpenJDK JVM implementations. The only sensitive bit is the Scala reflection mechanism used to detect columns.

YourKit Java Profiler
==================

![yourkit](https://s3-eu-west-1.amazonaws.com/websudos/oss/yklogo.png "YourKit Java Profiler")

We are very grateful to have the open source license support of YourKit, the most advanced Java profiler.

YourKit is the very core of our performance bottleneck testing, and without it phantom would still be a painfully slow tool.

[YourKit Java profiler](https://www.yourkit.com/java/profiler/index.jsp)
