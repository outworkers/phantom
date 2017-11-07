phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom?branch=develop) [![Coverage Status](https://coveralls.io/repos/github/outworkers/phantom/badge.svg?branch=develop)](https://coveralls.io/github/outworkers/phantom?branch=develop)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

Reactive type-safe Scala driver for Apache Cassandra/Datastax Enterprise

To stay up-to-date with our latest releases and news, follow us on Twitter: [@outworkers](https://twitter.com/outworkers_uk).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be open source, but the more adopters our projects have, the more people from our company will actively work to make them better.

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom")

Migrating to phantom 2.14.0 and using execution backends.
=========================================================

Please refer to the new docs on query execution to understand the breaking changes in phantom 2.14.0. They will
affect all users of phantom, as we further optimise the internals for better performance and to gently prepare 3.0.

Details [here](docs/querying/execution.md). In short, query generation is no longer coupled with query execution within
the framework. That means phantom can natively support different kind of concurrency frameworks in parallel, using
different sub-modules. That includes Monix, Twitter Util, Scala Futures, and a few others, some of which only available
via phantom-pro.

`import com.outworkers.phantom.dsl._` is now required in more places than before. The `future` method is no longer implementation by query classes, but
rather added via implicit augmentation by `QueryContext`. The return type of the `future` method is now dependent
on which `QueryContext` you use, so that's why importing is required, without it the necessary implicits will not
be in scope by default, or similarly, in some places new implicits are required to specify things specific to an execution backend.

Migrating to phantom 2.x.x series
=================================

The new series of phantom introduces several key backwards incompatible changes with previous versions.
This was done to obtain massive performance boosts and to thoroughly improve user experience with
phantom.

Read the [MIGRATION GUIDE](docs/migrate.md) for more information on how to upgrade.


Available modules
=================

This is a table of the available modules for the various Scala versions. Not all modules are available for all versions just yet, and this is because certain dependencies have yet to be published for Scala 2.12.

#### Phantom OSS

| Module name           | Scala 2.10.x        | Scala 2.11.x      | Scala 2.12.0      |
| ------------          | ------------------- | ------------------| ----------------- |
| phantom-connectors    | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-dsl           | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-jdk8          | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-sbt           | <span>yes</span>    | <span>no</span>  | <span>no</span>    |
| phantom-example       | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-thrift        | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-finagle       | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |
| phantom-streams       | <span>yes</span>    | <span>yes</span> | <span>yes</span>   |

#### Phantom Pro subscription edition

Modules marked with "x" are still in beta or pre-publishing mode.

| Module name           | Scala 2.10.x        | Scala 2.11.x      | Scala 2.12.x      | Release date   |
| ------------          | ------------------- | ------------------| ----------------- | -------------- |
| phantom-dse           | <span>yes</span>    | <span>yes</span>  | <span>yes</span>  | Released       |
| phantom-udt           | <span>yes</span>    | <span>yes</span>  | <span>yes</span>  | Released       |
| phantom-autotables    | <span>x</span>      | <span>x</span>    | <span>x</span>    | Released       |
| phantom-monix         | <span>x</span>      | <span>x</span>    | <span>x</span>    | Released       |
| phantom-docker        | <span>x</span>      | <span>x</span>    | <span>x</span>    | Released       |
| phantom-graph         | <span>x</span>      | <span>x</span>    | <span>x</span>    | April 2017     |
| phantom-spark         | <span>x</span>      | <span>x</span>    | <span>x</span>    | July 2017      |
| phantom-solr          | <span>x</span>      | <span>x</span>    | <span>x</span>    | July 2017      |
| phantom-migrations    | <span>x</span>      | <span>x</span>    | <span>x</span>    | September 2017 |
| phantom-native        | <span>x</span>      | <span>x</span>    | <span>x</span>    | December 2017  |
| phantom-java-dsl      | <span>x</span>      | <span>x</span>    | <span>x</span>    | December 2017  |

Using phantom
=============

### Scala 2.10, 2.11 and 2.12 releases ###

We publish phantom in 2 formats, stable releases and bleeding edge.

- The stable release is always available on Maven Central and will be indicated by the badge at the top of this readme. The Maven Central badge is pointing at the latest version

- Intermediary releases are available through our Bintray repo available at `Resolver.bintrayRepo("outworkers", "oss-releases")` or `https://dl.bintray.com/outworkers/oss-releases/`. The latest version available on our Bintray repository is indicated by the Bintray badge at the top of this readme.

### How phantom compares

To compare phantom to similar tools in the Scala/Cassandra category, you can read more [here](https://github.com/outworkers/phantom/blob/develop/comparison.md).

### Latest versions

The latest versions are available here. The badges automatically update when a new version is released.

- Latest stable version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) (Maven Central)
- Bleeding edge: [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg)](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) (OSS releases on Bintray)

<a id="learning-phantom">Tutorials on phantom and Cassandra</a>
======================================================================

For ease of use and far better management of documentation, we have decided to export the `README.md` to a compiled
documentation page, now available [here](https://github.com/outworkers/phantom/tree/develop/docs).

The following are the current resources available for learning phantom, outside of tests which are very useful in
highlighting all the possible features in phantom and how to use them.

This is a list of resources to help you learn phantom and Cassandra:

- [ ] [Quickstart](docs/quickstart.md)
- [ ] [Official documentation](docs/README.md)
- [ ] [Datastax Introduction to Cassandra](http://www.datastax.com/documentation/getting_started/doc/getting_started/gettingStartedIntro_r.html).
- [ ] [The official Scala API docs for phantom](http://phantom-docs.s3-website-eu-west-1.amazonaws.com/)
- [ ] [The main Wiki](https://github.com/outworkers/phantom/tree/develop/docs)
- [ ] The StackOverflow [phantom-dsl](http://stackoverflow.com/questions/tagged/phantom-dsl) tag, which we always monitor!
- [ ] Anything tagged phantom on our blog is a phantom tutorial: [phantom tutorials](http://outworkers.com/blog/tag/phantom)
- [ ] [A series on Cassandra: Getting rid of the SQL mentality](http://outworkers.com/blog/post/a-series-on-cassandra-part-1-getting-rid-of-the-sql-mentality)
- [ ] [A series on Cassandra: Indexes and keys](http://outworkers.com/blog/post/a-series-on-cassandra-part-2-indexes-and-keys)
- [ ] [A series on Cassandra: Advanced features](http://outworkers.com/blog/post/a-series-on-cassandra-part-3-advanced-features)
- [ ] [A series on phantom: Getting started with phantom](http://outworkers.com/blog/post/a-series-on-phantom-part-1-getting-started-with-phantom)
- [ ] [The Play! Phantom Activator template](https://github.com/outworkers/phantom-activator-template)
- [ ] [Thiago's Cassandra + Phantom demo repository](https://github.com/thiagoandrade6/cassandra-phantom)


<a id="issues-and-questions">Issues and questions</a>
=====================================================
<a href="#table-of-contents">back to top</a>

We love Cassandra to bits and use it in every bit of our stack. phantom makes it super trivial for Scala users to embrace Cassandra.

Cassandra is highly scalable and it is by far the most powerful database technology available, open source or otherwise.

Phantom is built on top of the [Datastax Java Driver](https://github.com/datastax/java-driver), which handles Cassandra connectivity
and raw query execution.

We are very happy to help implement missing features in phantom, answer questions about phantom, and occasionally help you out with Cassandra questions! Please use GitHub for any issues or bug reports.

Adopters
========

Here are a few of the biggest phantom adopters, though the full list is far more comprehensive.

![Microsoft](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/microsoft.png "Microsoft")
![CreditSuisse](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/creditsuisse.png "CreditSuisse")
![ING](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/ing.png "ING")
![UBS](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/ubs.png "UBS")
![Wincor Nixdorf](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/wincornixdorf.png "Wincor Nixdorf")
![Paddy Power](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/paddypower.png "Paddy Power")
![Strava](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/strava.png "Strava")
![Equens](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/equens.png "Equens")
![Pellucid Analytics](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/pellucid.png "Pellucid Analytics")
![Anomaly42](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/anomaly42.png "Anomaly42")
![ChartBoost](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/chartboost.png "Chartboost")
![Tecsisa](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/tecsisa.png "Tecsisa")
![Mobli](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/mobli.png "Mobli")
![VictorOps](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/victorops.png "Mobli")
![Socrata](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/socrata.png "Socrata")
![Sphonic](https://s3-eu-west-1.amazonaws.com/websudos/oss/adopters/sphonic.png "Sphonic")

License and copyright
======================

Phantom is distributed under the Apache V2 License.

- `Outworkers, Limited` is the copyright holder.

- You can use phantom in commercial products or otherwise.

- We strongly appreciate and encourage contributions.

- All paid for features are published and sold separately as `phantom-pro`, everything that is currently available for free will remain so forever.

If you would like our help with any new content or initiatives, we'd love to hear about it!

<a id="contributors">Contributors</a>
=====================================
<a href="#table-of-contents">back to top</a>

Phantom was developed at outworkers as an in-house project. All Cassandra integration at outworkers goes through phantom, and nowadays it's safe to say most Scala/Cassandra users in the world rely on phantom.

* Flavian Alexandru ([@alexflav23](https://github.com/alexflav23)) - maintainer
* Bartosz Jankiewicz ([@bjankie1](https://github.com/bjankie1))
* Benjamin Edwards ([@benjumanji](https://github.com/benjumanji))
* Kevin Wright ([@kevinwright](https://github.com/kevinwright))
* Eugene Zhulenev ([@ezhulenev](https://github.com/ezhulenev))
* Michal Matloka ([@mmatloka](https://github.com/mmatloka))
* Thiago Pereira ([@thiagoandrade6](https://github.com/thiagoandrade6))
* Juan José Vázquez ([@juanjovazquez](https://github.com/juanjovazquez))
* Viktor Taranenko ([@viktortnk](https://github.com/viktortnk))
* Stephen Samuel ([@sksamuel](https://github.com/sksamuel))
* Evan Chan ([@evanfchan](https://github.com/velvia))
* Jens Halm ([@jenshalm](https://github.com/jenshalm))
* Donovan Levinson ([@levinson](https://github.com/levinson))

<a id="copyright">Copyright</a>
===============================
<a href="#table-of-contents">back to top</a>

Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea, and special thanks to Miles Sabin and team behind
Shapeless, where we shamelessly stole all the good patterns from.

Copyright &copy; 2013 - 2017 outworkers.

Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome! Use GitHub for issues and pull requests and we will happily help out in any way we can!
