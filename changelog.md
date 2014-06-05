Changelog
=========

<a id="version-history">Version history</a>
===========================================

<ul>
    <li><a href="#version-0.6.0">0.6.0 - 11.05.2014</a></li>
    <li><a href="#version-0.7.0">0.7.0 - 22.05.2014</a></li>
    <li><a href="#version-0.8.0">0.8.0 - 04.06.2014</a></li>
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
- Vastly simplified structure of collection operators and removed ```com.newzly.phantom.thrift.Implicits._```
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
