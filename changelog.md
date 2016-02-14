Changelog
=========

<a id="version-history">Version history</a>
===========================================

<ul>
    <li><a href="#version-0.6.0">0.6.0 - 11.05.2014</a></li>
    <li><a href="#version-0.7.0">0.7.0 - 22.05.2014</a></li>
    <li><a href="#version-0.8.0">0.8.0 - 04.06.2014</a></li>
    <li><a href="#version-0.9.0">0.9.0 - 24.06.2014</a></li>
    <li><a href="#version-1.0.0">1.0.0 - 04.08.2014</a></li>
    <li><a href="#version-1.1.0">1.1.0 - 20.08.2014</a></li>
    <li><a href="#version-1.2.0">1.2.0 - 27.08.2014</a></li>
    <li><a href="#version-1.3.0">1.3.0 - 05.09.2014</a></li>
    <li><a href="#version-1.4.0">1.4.0 - 07.11.2014</a></li>
    <li><a href="#version-1.5.0">1.5.0 - 05.01.2015</a></li>
    <li><a href="#version-1.8.0">1.8.0 - 05.03.2015</a></li>
    <li><a href="#version-1.8.12">1.8.12 - 21.05.2015</a></li>
    <li><a href="#version-1.9.10">1.9.10 - 04.07.2015</a></li>
    <li><a href="#version-1.10.1">1.10.1 - 18.07.2015</a></li>
    <li><a href="#version-1.11.0">1.11.0 - 16.08.2015</a></li>
    <li><a href="#version-1.12.0">1.12.0 - 03.09.2015</a></li>
    <li><a href="#version-1.13.0">1.13.0 - 02.11.2015</a></li>
    <li><a href="#version-1.15.0">1.15.0 - 10.11.2015</a></li>
    <li><a href="#version-1.16.0">1.16.0 - 19.11.2015</a></li>
    <li><a href="#version-1.17.5">1.17.5 - 8.12.2015</a></li>
    <li><a href="#version-1.17.7">1.17.7 - 10.12.2015</a></li>
    <li><a href="#version-1.18.0">1.18.0 - 14.12.2015</a></li>
    <li><a href="#version-1.18.1">1.18.1 - 17.12.2015</a></li>
    <li><a href="#version-1.19.0">1.19.1 - 04.01.2016</a></li>
    <li><a href="#version-1.20.0">1.20.0 - 08.01.2016</a></li>
    <li><a href="#version-1.21.0">1.21.0 - 18.01.2016</a></li>
    <li><a href="#version-1.21.1">1.21.1 - 19.01.2016</a></li>
    <li><a href="#version-1.21.2">1.21.2 - 20.01.2016</a></li>
    <li><a href="#version-1.21.3">1.21.3 - 28.01.2016</a></li>
    <li><a href="#version-1.21.4">1.21.4 - 06.02.2016</a></li>
    <li><a href="#version-1.21.5">1.21.5 - 11.02.2016</a></li>
    <li><a href="#version-1.22.0">1.22.0 - 14.02.2016</a></li>
</ul>



<a id="version-0.6.0">0.6.0</a>
===============================

- Adding support for conditional tests.
- Added support for ```orderBy``` in select queries.
- Added support for Clustering columns.
- Added support for auto-generating Composite Keys.
- Added extensive tests for Secondary Indexes.
- Moved back to Scala reflection.
- Fixed field collection order.
- Added servlet container based tests to ensure Scala reflection is consistent.
- Simplifying Enumerators API.
- Added a changelog :)
- Added a table of contents in the documentation.
- Improved structure of test project.
- Added copyright information to a lot of files.
- Fixed license issue in Maven Central publishing license.
- Vastly simplified structure of collection operators and removed ```com.websudos.phantom.thrift.Implicits._```
- Fixed ```null``` handling bug in ```OptionalColumns```.

<a id="version-0.7.0">0.7.0</a>
===============================

- Added a variety of compilation tests for the phantom query builder. They test what should compile and what shouldn't, to verify compile time restrictions.
- Fixed support for Static columns.
- Bumped Twitter Scrooge version to 3.15.0
- Bumped Cassandra version to 2.0.7
- Fixed tests for TimeSeries
- Fixed tests for orderBy clauses.
- Fixed support for static columns.
- Fixed issues with using the ```setIdX``` List operator.
- Integrated Scalatra servlet container tests in the same test project.
- Removed phantom-scalatra-test module

<a id="version-0.8.0">0.8.0</a>
===============================

- PHANTOM-43: Added timestamp support for BatchStatements.
- PHANTOM-62: Added timestamp support for queries used in batch statements.
- PHANTOM-66: Added support for ```SELECT DISTINCT``` queries.
- PHANTOM-72: Added ability to enable/disable tracing on all queries.
- PHANTOM-75: Restructure the query mechanism to allow sharing methods easily.
- PHANTOM-76: Added a ```ifNotExists``` method to Insert statements.
- PHANTOM-78: Added ability to set a default consistency per table.
- PHANTOM-79: Added ability to chain multiple clauses on a secondary condition.
- PHANTOM-81: Fixed bug where count queries were always returning 1 because a ```LIMIT 1``` was improperly enforced.
- PHANTOM-82: Changed the return type of a count query from ```Option[Option[Long]]``` to ```Option[Long]```.
- PHANTOM-83: Enhanced phantom query builder to allow using ```where``` clauses for ```COUNT``` queries.
- PHANTOM-84: Added common methods to GitHub documentation.

<a id="version-0.9.0">0.9.0</a>
===============================

- PHANTOM-106: Fix broken links in the phantom documentation.

<a id="version-1.0.0">1.0.0</a>
===============================

- PHANTOM-108: Adding an example Cassandra connector.
- PHANTOM-112: Fixing race condition in the default ZooKeeper connector implementation


<a id="version-1.1.0">1.1.0</a>
===============================

- PHANTOM-105: The embedded Zookeeper client needs to connect before fetching data. Fixed connection state in Zk clients.
- PHANTOM-107: Add documentation for using our Zookeeper testing utilities.
- PHANTOM-113: Using a ZooKeeper Manager implementation to track availability and instantiation of ZooKeeper nodes.


<a id="version-1.2.0">1.2.0</a>
===============================

- PHANTOM-115: Fix SBT plugin dependency caused by ```publishSigned``` task in the GPG plugin. Kudos to Stephen Samuel.


<a id="version-1.3.0">1.3.0</a>
===============================

- PHANTOM-123: Fixed some documentation links.
- PHANTOM-127: Automatically collecting references to all UDT definitions.
- PHANTOM-131: Throwing an error when ```PrimaryKey``` and ```ClusteringOrder``` definitions are used in the same schema.
- PHANTOM-134: Allowing users of ```DefaultZooKeeperManager``` to specify their own timeout for ZooKeeper connections.
- PHANTOM-136: Added UDT examples to the UDT example module.
- PHANTOM-109: Added a reference collector to all fields of an UDT.

<a id="version-1.4.0">1.4.0</a>
===============================

- Fixed more broken links in the documentation and added table with Future API methods.
- Implemented UDT columns and added serialisation tests.
- Moved syscall to get available processors to a one time only init for performance improvements.
- Fixed ```BatchStatement.apply``` from ignoring the argument list passed to the method.
- Fixed ```DefaultClusterStore``` to allow overriding connection timeouts and the CQL query used to initialise keyspaces.
- Allowing users to override how ports are parsed once retrieved from ZooKeeper.
- Allowing users to override cluster builders with their own custom implementation.
- PHANTOM-93: Finalising ScalaStyle build integration.

<a id="version-1.5.0">1.5.0</a>
===============================

- Fixed several typos and broken links in the README.
- Added documentation for resolver requirements at the top of the repository.
- Added clarification for Scala 2.11 release.
- Improved UDT implementation.
- Heavy performance improvements for iterator processing (thanks @benjumanji for diligently profiling every bit).
- Added full support for Scala 2.11.

<a id="version-1.8.0">1.8.0</a>
===============================



<a id="version-1.8.12">1.8.12</a>
===============================

- Fixed encoding support for strings containing a single quote `'`.
- Bumped util library dependency to `0.8.8` to include small bug fixes and more robust testing in the util library.
- A custom patience timeout for `successful` calls on Futures has been created to replace the now obsolete implicit from `com.websudos.util.testing._`.
- Bumped Twitter Finagle deps to `6.25.0` and Twitter Util to `6.24.0`.
- Removed hard coded list lengths from `TimeSeriesTest` in favour of named variables. This compensates for the fix in the util library, where before `0.8.4` a call to `genList[T](n)` would generate `n - 1`elements instead of `n`. This has been fixed and phantom updated to compensate for all changes.
- Bumped `cassandra-unit` version to `2.0.2.6` as the newer version is available on our public Bintray repository.
- Removed Websudos Artifactory resolvers and replaced with Bintray configuration.
- Added a Bintray version badge to automatically show the latest available Bintray version on the GitHub README.
- Separated performance related tests written with ScalaMeter into a new configuration called `perf`. Tests can now be semantically distinguished by their purpose and `sbt:test` will not run performance tests by default. Instead, benchmarks are run using `perf:test` exclusively, which fixes `scoverage` integration during the Travis CI phase.
- Fixed a few deadlinks in the `README.md`.
- Removed `Unmodifiable` trait market from the implementation of `Index`, which now allows users to update the value of secondary keys.
- Separated `DELETE` query serialization concerns into a specialized builder called `QueryBuilder.Delete`.
- Added tests for `DELETE` query serialisation and for the new `QueryBuilder.Delete`.
- Removed fixed Thrift dependency that was enforcing `org.apache.thrift % libthrift % 0.9.1` from the `phantom-thrift` module. Consumers of Thrift modules can now set their own Scrooge and Thrift version without `phantom-thrift` interfering with them and causing serialization problems.
- Added a `RootThriftPrimitive` to allow easily creating an implicit primitive for custom types. This is used when Thrift columns are part of the primary key.
- Moved the duplicate `package.scala` from the test part of `com.websudos.phantom.thrift` to avoid strange overloading of imports between the main module and test module. Also fixes compilation warning message about conflicting members.
- Fixed support for nested Primitive types. A `Column[Owner, Record, T` can now de-serialize to a type that is completely different from `T`. This fixes edge scenarios like the `DateTimePrimitive` of type `Primitive[org.joda.time.DateTime]` which has no proprietary extractor and is instead just a thin layer around the existing `java.util.Date` extractor. Phantom can now feed in the correct extraction type and deserialize columns like `MapColumn[Owner, Record, DateTime, String]`.
- Bumped ScalaTest dependency to version `2.2.4`.
- Fixed serialization of Blob columns by using Datastax Java Driver helper object `Bytes`.
- Added support for SBT version management via Twitter's `sbt-package-dist` published via the custom Websudos fork: `"com.websudos" %% "sbt-package-dist" % "1.2.0"`.
- Bumped Scala version to `2.10.5` and `2.11.6`.
- Added dependency resolution retry during Travis CI phase. Travis will now retry to fetch dependencies 3 times before giving up, which fixes most timeout errors during dependency resolution and lets builds consistently pass.
- Moved publishing infrastructure to Bintray and added a dependency on `"me.lessis" %% "bintray-sbt" % "0.3.0"` to publish artefacts to Bintray.
- Bumped SBT version to `0.13.8` and bumped `net.virtualvoid.dependencygraph` version to `0.7.5` in `plugins.sbt`.


<a id="version-1.8.13">1.8.13</a>
===============================

- Removed `TestZookeeperConnector` from the default `phantom-testkit`.
- Removed `BaseTest` and `FeatureBaseTest` from `phantom-testkit`.
- Removed dependency on `phantom-zookeeper` from the default implementation of `phantom-connectors`. This was a very bad accident and Zk dependencies were being pulled in even if the end user wasn't relying on ZooKeeper for service discovery.

<a id="version-1.10.0">1.10.0</a>
===============================

- Bumped Scala version to 2.11.7 for the 2.11.x releases.
- Re-added ability to set consistency levels in every query except Batch queries.
- Moved implementation of query execution back to simple statements in the Datastax Java driver.
- Allowing Datastax Java driver to set the consistency level and control final levels of serialization.
- Added ability to change CQL serialization based on CQL protocol version.


<a id="version-1.11.0">1.11.0</a>
================================

- Changed the serialization technique used for consistency levels to use a Scala `Option[ConsistencyLevel]` definition
instead of a nullable field.
- Added support for protocol level specification of consistency levels inside `BatchStatement`.
- Removed most of the remaining documentation from the GitHub readme and placed in the Wiki.
- Added a list of tutorials for using phantom, including basic guidelines.
- Added a default forwading mechanism for a `KeySpace` definition inside the `Connector` obtained via `KeySpaceDef`.
- Upgraded to use Diesel engine for query operations.
- Upgraded to use Diesel engine for multi-part queries.

<a id="version-1.12.0">1.12.0</a>
================================

- Removed support for `java.util.Date` in the default date columns. This has been removed in Cassandra 2.2.
- Replaced default date parsing from `java.util.Date` with `com.datastax.driver.core.LocalDate` primitive.
- Added a `LocalDate` column to offer complementary support for `com.datastax.driver.core.LocalDate`, `java.util.Date`
and `org.joda.time.DateTime`.
- Replaced `BatchQuery` serialization to use `com.datastax.driver.core.BatchStatement` internally.
- Removed superfluous check in `ExecutableQuery` for nullable consistency level definitions.

<a id="version-1.13.0">1.13.0</a>
================================

- Added support for `maxTimeuuid` and `minTimeuuid` in query columns.
- Added support for multiple `orderBy` statements in a single clause.
- Removed implementation of `SessionProvider` in favour of a single global session object inside phantom.
- Fixed implementation of logging by using logf4j12-over-slf4j bridge and logback-classic.
- Removed IPv6 connectivity support from the Travis build.
- Added `.jvmopts` to the Travis build configuration to prevent automated attempts to IPv6 connections in the Datastax driver.
- Removed a lot of `.get` accessor calls in the test methods in favour of the idiomatic `OptionValues` DSL from ScalaTest.

<a id="version-1.15.0">1.15.0</a>
================================

- Added support for Reactive Streams in phantom, with a default mapper subscriber.
- Implemented a publisher for reactive streams based on Play Enumerators convertions.

<a id="version-1.16.0">1.16.0</a>
================================

- Fixed buffer overflow issue in Reactive streams.
- Added tests for the reactive streams publisher.
- Added support for binding in INSERT JSON clauses.
- Forcing request builders in reactive streams to be `Batchable`.
- Allowing elements to be derived from `Throwable` by adding an `ErrorWrapper` to error propagation in actors.
- Adding support for binding `scala.Enumeration#Value` in prepared statements.
- Bumped `xsbt-web-plugin` to `2.0.4`.

<a id="version-1.17.5">1.17.5</a>
================================

- Fixed support for token operators for multiple tokens.
- Fixed support for binding single arguments to a prepared statement by adding specialized 1 arg methods.
- Adding better comments to the basic implementation.
- Removed unused dead code in operator query columns for multi-column token operators.
- Deprecated shutdown method on the main `Manager`.

<a id="version-1.17.7">1.17.7</a>
================================

- Fixed serialization of `InsertQuery` builders that specify a using clause.
- Fixed serialization of `InsertJsonQuery` builders that specify a using clause.
- Fixed primitive serialization inside `PreparedStatements`.
- Added primitive parsing of `BigDecimal` inside prepared statements.
- Added support for binding lists inside prepared statements.
- Fixed TTL support in `UpdateClause`.
- Fixed protocol version comparisons to address non standard greater than 1 results in `compareTo` implementation.

<a id="version-1.18.0">1.18.0</a>
================================

- Added a query options DSL to fix using `consistencyLevel` settings in `SelectQuery`.
- Added a `QueryOptions` DSL to replace `Option[ConsistencyLevel]` setting in `ExecutableQuery`.
- Bumped major version to `1.18.0` to address backwards incompatible change.
- Fixed serialization issue in defining `LeveledCompactionStrategy`.

<a id="version-1.18.1">1.18.1</a>
================================

- Adding the ability to use `gt`, `gte`, `lt`, `lte` and `isNot` operators with a conditional update clause.
- Bumping the version to `1.18.1`.

<a id="version-1.19.0">1.19.0</a>
================================

- Deprecated and completely removed the `phantom-teskit` module as it is now obsolete.
- Upgraded the entire set of tests to use the new `Database` API.
- Added support for updating specific entries in a `MapColumn`.
- Added support for deleting specific entries from a `MapColumn`.
- Fixed timeout configuration for ScalaTest based entries.
- Updated examples to showcase new Database API.

<a id="version-1.19.1">1.19.1</a>
================================

- Fixed `maxTimeUUID` and `minTimeUUID` implementations to use proper serialization for `TimeUUID` range queries.

<a id="version-1.20.0">1.20.0</a>
================================

- Renamed all CAS comparison operators to match `is$Op` format.
- Added `ExecutableCreateStatementsList` to fix index creation issues inside `Database.autocreate`.
- Added support for Map entry comparisons and the `Entries` mixin trait for map columns with secondary indexes.

<a id="version-1.21.0">1.21.0</a>
================================

- Added support for preparing `UpdateQuery` via `p_where` -> `p_and` pairs and `p_modify` -> `p_and` pairs.
- Added more tests for prepared delete queries and update queries.
- Added the ability to set more options on a statement before executing it.
- Added ability to prepare map entry update clauses.
- Bumped util library version to `0.10.8`.
- Bumped Datastax Java Driver version to `3.0.0-rc1` and using `new SimpleStatement` constructor in favour the now
removed `session.newSimpleStatement`.
- Added ability to modify a `statement` directly the execution operation to set options via the Java DSL.
- Added support for `Table.select.function(t => fn(t.column)`, including `dateOf`, `unixTimestampOf` and `writetime`.
- Increased default schema synchronisation timeout to 10 seconds inside phantom tests.
- Added `sbt-sonatype` plugin to re-enable Maven Central syncing.

<a id="version-1.21.1">1.21.1</a>
================================

- Added ability to merge batch queries.
- Adding support for `iterator` method on queries.

<a id="version-1.21.1">1.21.2</a>
================================

- Fixing serialization of Compaction properties in CREATE and ALTER queries.

<a id="version-1.21.3">1.21.3</a>
================================

- Fixing type evidence required for `p_and` chains in `UpdateQuery`.

<a id="version-1.21.4">1.21.4</a>
================================

- #414 Added ablity to use prepared statements inside of batch statemets.
- #417 Corrected cql type for LocalDate and Date accessor method.
- #412 Bumped Datastax Java Driver version to 3.0.0

<a id="version-1.21.5">1.21.5</a>
================================
- Added SmallInt and TinyInt support.
- Added a new contributor to the list.

<a id="version-1.22.0">1.22.0</a>
================================

- PHANTOM-194: Fixed serialization of ```Table.select.distinct``` queries.
- Added support for `LocalDate` columns.
- Added `driver-extras` dependency from the Datastax set.