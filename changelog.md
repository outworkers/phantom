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
