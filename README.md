phantom [![Build Status](https://travis-ci.org/newzly/phantom.png?branch=develop)](https://travis-ci.org/newzly/phantom)
==============
Asynchronous Scala DSL for Cassandra


Using phantom
=============

The current version is: ```val phantomVersion = 0.1.12```.
Phantom is published to Maven Central and it's actively and avidly developed.


Integrating phantom in your project
===================================

For most things, all you need is ```phantom-dsl```. Read through for information on other modules.

```scala
libraryDependencies ++= Seq(
  "com.newzly"  %% "phantom-dsl"                   % phantomVersion
)
```

The full list of available modules is:

```scala
libraryDependencies ++= Seq(
  "com.newzly"  %% "phantom-dsl"                   % phantomVersion,
  "com.newzly"  %% "phantom-cassandra-unit"        % phantomVersion,
  "com.newzly"  %% "phantom-example"               % phantomVersion,
  "com.newzly"  %% "phantom-thrift"                % phantomVersion,
  "com.newzly"  %% "phantom-test"                  % phantomVersion,
  "com.newzly"  %% "phantom-finagle"               % phantomVersion
)
```


Data modeling with phantom
==========================
  
```scala

import java.util.{ UUID, Date }
import com.datastax.driver.core.Row
import com.newzly.phantom.sample.ExampleModel
import com.newzly.phantom.Implicits._

case class ExampleModel (
  id: Int,
  name: String,
  props: Map[String, String],
  timestamp: Int,
  test: Option[Int]
)

sealed class ExampleRecord private() extends CassandraTable[ExampleRecord, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord, ExampleModel, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}

```

Querying with Phantom
=====================

The query syntax is inspired by the Foursquare Rogue library and aims to replicate CQL 3 as much as possible.

Phantom works with both Scala Futures and Twitter Futures. For the Twitter flavour, simply add the ```"com.newzly  %% phantom-finagle % phantomVersion"``` dependency.

```scala

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"

  // now define a session, a normal Datastax cluster connection
  implicit val session = SomeCassandraClient.session;
  
  def getRecordsByName(name: String): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).fetch
  }
  
  def getOneRecordByName(name: String, someId: UUID): Future[Option[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).and(_.id eqs someId).one()
  }
}
```

Partial selects
===============

All partial select queries will return Tuples and are therefore limited to 22 fields.
This will change in Scala 2.11 and phantom will be updated once cross version compilation is enabled.

```scala
  def getNameById(id: UUID): Future[Option[String]] = {
    ExampleRecord.select(_.name).where(_.id eqs someId).one()
  }
  
  def getNameAndPropsById(id: UUID): Future[Option(String, Map[String, String])] {
    ExampleRecord.select(_.name, _.props).where(_.id eqs someId).one()
  }
```

Collection operators
====================

phantom supports CQL 3 modify operations for CQL 3 collections: ```list, set, map```.

It works as you would expect it to:

List operators: ```prepend, prependAll, append, appendAll, remove, removeAll```

```scala

ExampleRecord.update.where(_.id eqs someId).modify(_.someList prepend someItem).future()
ExampleRecord.update.where(_.id eqs someId).modify(_.someList prependAll someItems).future()

ExampleRecord.update.where(_.id eqs someId).modify(_.someList append someItem).future()
ExampleRecord.update.where(_.id eqs someId).modify(_.someList appendAll someItems).future()

ExampleRecord.update.where(_.id eqs someId).modify(_.someList remove someItem).future()
ExampleRecord.update.where(_.id eqs someId).modify(_.someList removeAll someItems).future()

```

Set operators: ```append, appendAll, remove, removeAll```
Map operators: ```put, putAll```

For working examples, see [ListOperatorsTest.scala](https://github.com/newzly/phantom/blob/develop/phantom-test/src/test/scala/com/newzly/phantom/dsl/crud/ListOperatorsTest.scala) and [MapOperationsTest.scala](https://github.com/newzly/phantom/blob/develop/phantom-test/src/test/scala/com/newzly/phantom/dsl/crud/MapOperationsTest.scala).


Automated schema generation
===========================

Replication strategies and more advanced features are not yet available in phantom, but CQL 3 Table schemas are  automatically generated from the Scala code. To create a schema in Cassandra from a table definition:

```scala

import scala.concurrent.Await
import scala.concurrent.duration._

Await.result(ExampleRecord.create().future(), 5000 millis)
```

Of course, you don't have to block unless you want to.


Partition tokens, token functions and paginated queries
======================================================

```scala

import scala.concurrent.Await
import scala.concurrent.duration._
import com.newzly.phantom.Implicits._

sealed class ExampleRecord2 private() extends CassandraTable[ExampleRecord2, ExampleModel] with LongOrderKey[ExampleRecod2, ExampleRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this)
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}


val orderedResult = Await.result(Articles.select.where(_.id gtToken one.get.id ).fetch, 5000 millis)

```
For more details on how to use Cassandra partition tokens, see [SkipRecordsByToken.scala]( https://github.com/newzly/phantom/blob/develop/phantom-test/src/test/scala/com/newzly/phantom/dsl/SkipRecordsByToken.scala)


Cassandra Time Series
=====================

phantom supports Cassandra Time Series with both ```java.util.Date``` and ```org.joda.time.DateTime ```. To use them, simply mixin ```com.newzly.phantom.keys.ClusteringOrder``` and either ```Ascending``` or ```Descending```.

Restrictions are enforced at compile time.

```scala

import com.newzly.phantom.Implicits._

sealed class ExampleRecord3 private() extends CassandraTable[ExampleRecord3, ExampleModel] with LongOrderKey[ExampleRecod3, ExampleRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```

Automatic schema generation can do all the setup for you.


Composite keys
==============
Phantom also supports using composite keys out of the box. The schema can once again by auto-generated.

A table can have only one ```PartitionKey``` but several ```PrimaryKey``` definitions. Phantom will use these keys to build a composite value. Example scenario, with the composite key: ```(id, timestamp, name)```

```scala

import org.joda.time.DateTime
import com.newzly.phantom.Implicits._

sealed class ExampleRecord3 private() extends CassandraTable[ExampleRecord3, ExampleModel] with LongOrderKey[ExampleRecod3, ExampleRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with PrimaryKey[DateTime]
  object name extends StringColumn(this) with PrimaryKey[String]
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```

CQL 3 index and non-primary index columns
=========================================

When you want to use a column in a ```where``` clause, you need an index on it. Cassandra data modeling is out of the scope of this writing, but phantom offers ```com.newzly.phantom.Keys.SecondaryKey``` to enable querying.

The CQL 3 schema for secondary indexes can also be auto-generated with ```ExampleRecord4.create()```.

```scala

import org.joda.time.DateTime
import com.newzly.phantom.Implicits._

sealed class ExampleRecord4 private() extends CassandraTable[ExampleRecord4, ExampleModel] with LongOrderKey[ExampleRecod4, ExampleRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with SecondaryKey[DateTime]
  object name extends StringColumn(this) with SecondaryKey[String]
  object props extends MapColumn[ExampleRecord2, ExampleRecord, String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}
```


Asynchronous iterators for large record sets
============================================

Phantom comes packed with CQL rows asynchronous lazy iterators to help you deal with billions of records.
phantom iterators are based on Play iterators with very lightweight integration.

The functionality is identical with respect to asyncrhonous, lazy behaviour and available methods.
For more on this, see this [Play tutorial](
http://mandubian.com/2012/08/27/understanding-play2-iteratees-for-normal-humans/)


Usage is trivial:

```scala

import scala.concurrent.Await
import scala.concurrent.duration._
import com.newzly.phantom.Implicits._

val enumerator = Await.result(ExampleRecord.select.where(_.timestamp gtToken someTimestamp).fetchEnumerator(), 5000 millis)

```

Batch statements
================

phantom also brrings in support for batch statements. To use them, see [IterateeBigTest.scala]( https://github.com/newzly/phantom/blob/develop/phantom-test/src/test/scala/com/newzly/phantom/iteratee/IterateeBigTest.scala)

We have tested with 10,000 statements per batch, and 1000 batches processed simulatenously. Before you run the test, beware that it takes ~40 minutes.

Batches use lazy iterators and daisy chain them to offer thread safe behaviour. They are not memory intensive and you can expect consistent processing speed even with 1 000 000 statements per batch.


Thrift integration
==================

We use Apache Thrift extensively for our backend services. ```phantom``` is very easy to integrate with Thrift models and uses ```Twitter Scrooge``` to compile them. Thrift integration is optional and available via ```"com.newzly" %% "phantom-thrift"  % phantomVersion```.

```thrift
namespace java com.newzly.phantom.sample.ExampleModel

stuct ExampleModel {
  1: required i32 id,
  2: required string name,
  3: required Map<string, string> props,
  4: required i32 timestamp
  5: optional i32 test
}
```


Running the tests
=================

phantom uses Embedded Cassandra to run tests without a local Cassandra server running.
You need two terminals to run the tests, one for Embedded Cassandra and one for the actual tests.

```scala
sbt
project phantom-cassandra-unit
run
```

Then in a new terminal

```scala
sbt
project phantom-test
test
```

Maintainers
===========

Phantom was developed at newzly as an in-house project.
All Cassandra integration at newzly goes through Phantom.

- Sorin Chiprian sorin.chiprian@newzly.com
- Flavian Alexandru flavian@newzly.com

Pre newzly fork
===============
Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Copyright
=========
Copyright 2013 WhiskLabs, Copyright 2013 - 2014 newzly.


Contributions
=============

Contributions are most welcome! 

To contribute, simply submit a "Pull request" via GitHub.

We use GitFlow as a branching model and SemVer for versioning.

