phantom
[![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom) [![Coverage Status](https://coveralls.io/repos/outworkers/phantom/badge.svg)](https://coveralls.io/r/outworkers/phantom)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/websudos/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/websudos/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.websudos/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.websudos/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
===============================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================================

Reactive type-safe Scala driver for Apache Cassandra/Datastax Enterprise

To stay up-to-date with our latest releases and news, follow us on Twitter: [@outworkers](https://twitter.com/outworkers_uk).

If you use phantom, please consider adding your company to our list of adopters.
phantom is and will always be [freeware](https://en.wikipedia.org/wiki/Freeware), but the more adopters our projects have, the more people from our company will actively work to make them better.

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom")


Using phantom
=============

### Scala 2.10 and 2.11 releases ###

We publish phantom in 2 formats, stable releases and bleeding edge.

- The stable release is always available on Maven Central and will be indicated by the badge at the top of this readme. The Maven Central badge is pointing at the latest version

- Intermediary releases are available through our managed Bintray repository available at `https://dl.bintray.com/websudos/oss-releases/`. The latest version available on our Bintray repository is indicated by the Bintray badge at the top of this readme.


### How phantom compares

To compare phantom to similar tools in the Scala/Cassandra category, you can read more [here](https://github.com/outworkers/phantom/blob/develop/comparison.md).

### Latest versions

The latest versions are available here. The badges automatically update when a new version is released.

- Latest stable version: [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.websudos/phantom-dsl_2.11) (Maven Central)
- Bleeding edge: [![Bintray](https://api.bintray.com/packages/websudos/oss-releases/phantom-dsl/images/download.svg)](https://bintray.com/websudos/oss-releases/phantom-dsl/_latestVersion) (OSS releases on Bintray)

### Roadmap to Phantom 2.0.0

Phantom's next major release is slowly approaching completion, and if you would like to know more about what's to come and about what we have in store for you next, have a look at the below list. Feedback and contributions are welcome, and we are happy to prioritise any crucial features Phantom may currently be lacking.

#### Licensing and distribution

- [x] Revert all Outworkers projects and all their dependencies to the Apache V2 License. 
- [ ] Publish `outworkers-util` and all sub modules to Maven Central.
- [ ] Publish `outworkers-diesel` and all sub modules to Maven Central.
- [ ] Remove all non standard resolvers from Phantom, all dependencies should build from JCenter and Maven Central by default with no custom resolvers required. 

#### Macro API to replace runtime features

- [x] Replace the Scala reflection library with a macro that can figure out what the contents of a table are.
- [x] Generate the name of a table using macros.
- [x] Generate the primary key of a table using macros.
- [x] Enforce primary key restrictions on a table using a macro.
- [x] Generate the `fromRow` method of `CassandraTable` using a macro if the `case class` fields and `table` columns are matched.
- [ ] Enforce a same ordering restriction for case class fields and table columns to avoid generating invalid methods with the macro.
- [ ] Generate the `fromRow` if the fields match, they are in abitrary order, but there are no duplicate types.
- [ ] Allow arbitrary inheritance and usage patterns for Cassandra tables, and resolve inheritance resolutions with macros to correctly identify desired table structures.

#### Tech debt

- [ ] Correctly implement Cassandra pagination using iterators, currently setting a `fetchSize` on a query does not correctly propagate or consume the resulting iterator, which leads to API inconsistencies and `PagingState` not being set on any `ResultSet`.
- [ ] Add a build matrix that will test phantom against multiple versions of Cassandra in Travis for Scala 2.11, with support for all major releases of Cassandra.
- [ ] Bump code coverage up to 100%

#### Features

- [ ] Native support for multi-tenanted environments via cached sessions.
- [ ] Case sensitive CQL.
- [ ] Materialized views.
- [ ] SASI index support
- [ ] Support for `PER PARTITION LIMIT` in `SelectQuery`.
- [ ] Support for `GROUP BY` in `SelectQuery`.

#### Scala 2.12 support

- [ ] Add support for Scala 2.12 in the `util` library, remove all dependencies that don't comply.
- [x] Add support for Scala 2.12 in the `diesel-engine`.
- [ ] Add support for Scala 2.12 in `phantom-dsl`
- [ ] Add support for Scala 2.12 in `phantom-connectors`
- [ ] Add support for Scala 2.12 in `phantom-reactivestreams`
- [ ] Add support for Scala 2.12 in `phantom-finagle`

#### Documentatiom

- [ ] Move documentation back to the docs folder.
- [ ] Add a documentation website on the main page.
- [ ] Create a navigator that allows viewing the documentation at a particular point in time.

<a id="learning-phantom">Tutorials on phantom and Cassandra</a>
======================================================================

For ease of use and far better management of documentation, we have decided to export the `README.md` to a proper
Wiki page, now available [here](https://github.com/outworkers/phantom/wiki/).

The following are the current resources available for learning phantom, outside of tests which are very useful in
highlighting all the possible features in phantom and how to use them.

This is a list of resources to help you learn phantom and Cassandra:

- [ ] [Datastax Introduction to Cassandra](http://www.datastax.com/documentation/getting_started/doc/getting_started/gettingStartedIntro_r.html).
- [ ] [The official Scala API docs for phantom](http://phantom-docs.s3-website-eu-west-1.amazonaws.com/)
- [ ] [The main Wiki](https://github.com/outworkers/phantom/wiki)
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

Phantom is built on top of the [Datastax Java Driver](https://github.com/datastax/java-driver), which does most of the heavy lifting.

We are very happy to help implement missing features in phantom, answer questions about phantom, and occasionally help you out with Cassandra questions! Please use GitHub for any issues or bug reports.

Adopters
========

Some of the companies using phantom:

- [CreditSuisse](https://www.credit-suisse.com/global/en/)
- [ING](http://www.ing.com/en.htm)
- [UBS](https://www.ubs.com/global/en.html)
- [Wincor Nixdorf](http://www.wincor-nixdorf.com/internet/site_EN/EN/Home/homepage_node.html)
- [Paddy Power](http://www.paddypower.com/)
- [Mobli](https://www.mobli.com/)
- [Pellucid Analytics](http://www.pellucid.com/)
- [Equens](http://www.equens.com/)
- [outworkers](https://www.outworkers.com/)
- [VictorOps](http://www.victorops.com/)
- [Socrata](http://www.socrata.com)
- [Sphonic](http://www.sphonic.com/)
- [Anomaly42](http://www.anomaly42.com/)
- [Tecsisa](http://www.tecsisa.com/en/)
- [Tuplejump](http://www.tuplejump.com/)
- [FiloDB](http://www.github.com/tuplejump/FiloDB) - the fast analytics database built on Cassandra and Spark
- [Chartboost](https://www.chartboost.com)


License and copyright
======================

Phantom is [freeware software](https://en.wikipedia.org/wiki/Freeware) and uses a proprietary license that in plain English says the following:

- Phantom is the intellectual property of `Outworkers`, it is not provided under an OSS license.

- You can use phantom in commercial products or otherwise, so long as you use one of the official versions available on Bintray or Maven Central.

- You are not allowed to distribute altered copies of phantom in binary form.

- You cannot offer paid for training on phantom unless you are a direct partner to `Outworkers` and you have a written intellectual property agreement in place with us.

- If you simply have a `Build.scala` or `build.sbt` dependency on phantom, you have nothing to worry about.

- All paid for features are published and sold separately as `phantom-pro`, everything that is currently available for free will remain so forever.

If you would like our help with any new content or initiatives, we'd love to hear about it!

<a id="contributors">Contributors</a>
=====================================
<a href="#table-of-contents">back to top</a>

Phantom was developed at outworkers as an in-house project. All Cassandra integration at outworkers goes through phantom, and nowadays it's safe to say most
Scala/Cassandra users in the world rely on phantom.

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

Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Copyright &copy; 2013 - 2016 outworkers.

Contributing to phantom
=======================
<a href="#table-of-contents">back to top</a>

Contributions are most welcome! Use GitHub for issues and pull requests and we will happily help out in any way we can!
