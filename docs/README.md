phantom [![Build Status](https://travis-ci.org/outworkers/phantom.svg?branch=develop)](https://travis-ci.org/outworkers/phantom) [![Coverage Status](https://coveralls.io/repos/outworkers/phantom/badge.svg)](https://coveralls.io/r/outworkers/phantom)  [![Codacy Rating](https://api.codacy.com/project/badge/grade/25bee222a7d142ff8151e6ceb39151b4)](https://www.codacy.com/app/flavian/phantom_2) [![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.outworkers/phantom-dsl_2.11) [![Bintray](https://api.bintray.com/packages/outworkers/oss-releases/phantom-dsl/images/download.svg) ](https://bintray.com/outworkers/oss-releases/phantom-dsl/_latestVersion) [![ScalaDoc](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11.svg?label=scaladoc)](http://javadoc-badge.appspot.com/com.outworkers/phantom-dsl_2.11) [![Gitter](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/outworkers/phantom?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
=============================================================================================================
Reactive type-safe Scala driver for Apache Cassandra/Datastax Enterprise

![phantom](https://s3-eu-west-1.amazonaws.com/websudos/oss/logos/phantom.png "Outworkers Phantom")

To stay up-to-date with our latest releases and news, follow us on Twitter: [@outworkers](https://twitter.com/outworkers_uk).

If you use phantom, please consider adding your company to our list of adopters. Phantom is and will always be completely free and open source, under the Apache V2 license. Adopters encourage further adoption and a wider community around the project, which is quid pro quo for everyone involved.

Roadmap
========

### Phantom OSS

While dates are not fixed, we will use this list to tell you about our plans for the future. If you have great ideas about what could benefit all phantom 
adopters, please get in touch. We are very happy and eager to listen.

- [x] Prepared statements(available as of 1.15.0)

- [x] A new QueryBuilder(available as of 1.6.0)

- [x] Zookeeper support(available as of 1.1.0).

- [x] Reactive streams support(available as of 1.15.0)

- [x] Improved DSL, complete separation of reactive streams into separate module(available as of 1.26.0)

- [x] Further cleanup of build, making project more lightweight, separating Twitter primitives support into `phantom-finagle`. (1.26.0)


#### Phantom 1.30.x

- [x] Comparison of Phantom versus other established tools. 

- [ ] Support for case sensitive Cassandra tables and keyspace names.

- [ ] Support for tuple columns and collection columns

- [x] Support for frozen collection columns as primary columns.

- [ ] Deeper integration of CQL features, such as advanced `USING` clauses, `GROUP BY` and `PER PARTITION LIMIT`.(2.0.0)

- [ ] SASI Index support.

- [x] A `KeySpaceBuilder` that allows type safe specification of keyspace creation properties.(available as of 1.29.4)


#### Phantom 2.0.0

- [x] Removing `scala-reflect.jar` from `build.sbt`, replacing the DSL mechanism with a macro based one.

- [ ] Added Scala 2.12 support.

- [x] Added native support for `TupleColumn`, with implicit macro generation.

- [x] Added support for `TimeWindowCompaction`.

- [ ] Add support for `GROUP BY` and `PER PARTITION LIMIT`.

- [ ] Moving documentation back to a branch and to a dedicated versioned website based on Git tags.

- [ ] Added implicit.ly integration to notify our audience of new releases.

- [ ] Bringing test coverage to 100%(2.0.0)

- [ ] Ability to specify application wide configuration, such as case sensitive column names.

- [ ] A new website with highly improved documentation, as well as a per version docs browser.

- [x] Replacing Travis Cassandra service with CCM to natively include multi-version testing in our setup.

- [x] Macro derived primitives for complex types.

### Phantom Pro

#### v.1.0

- [x] Full support for UDTs.
- [x] Support for UDT collection types.
- [ ] Scala language based User defined functions.
- [ ] Scala language based User defined aggregates.
- [ ] Cassandra 3.8+ support.
- [ ] Materialised views.
- [ ] Development automated schema migrations.

#### 1.1.0
- [ ] Auto-tables, ability to generate queries entirely of out `case class` definitions.
- [ ] Advanced table migrations.

Commercial support
===================
<a href="#table-of-contents">back to top</a>

We, the people behind phantom, run a software development house specialising in Scala and NoSQL. If you are after enterprise grade
training or support for using phantom, [Outworkers](http://outworkers.com) is here to help!

We offer a comprehensive range of elite Scala development services, including but not limited to:

- Software development
- Remote contractors for hire
- Advanced Scala and Cassandra training

We are huge advocates of open source and we will open source every project we can! To read more about our open source efforts, click [here](http://www.outworkers.com/work).



