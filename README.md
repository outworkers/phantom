phantom
==============
Asynchronous Scala DSL for Cassandra


Using phantom
=============

The current version is: ```0.1.0```.

```scala
resolvers ++= Seq("newzly Releases" at "http://maven.newzly.com/repository/internal")
libraryDependencies ++= Seq(
  "com.newzly"  %% "phantom-dsl"    % "0.1.0",
  "com.newzly"  %% "phantom-thrift" % "0.1.0"
)
```

[![Build Status](https://travis-ci.org/newzly/phantom.png?branch=develop)](https://travis-ci.org/newzly/phantom)

Basic data models and Thrift IDL definitions
======================

We use Apache Thrift extensively for our backend services. ```phantom``` is very easy to integrate with Thrift models and uses ```Twitter Scrooge``` to compile them. Thrift integration is optional and available via ```"com.newzly" %% "phantom-thrift"  % "0.1.0"```.

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

If you don't want Thrift integration, you can simply use:
```scala
case class ExampleModel (
  id: Int,
  name: String,
  props: Map[String, String],
  timestamp: Int,
  test: Option[Int]
)
```

Data modeling with phantom
==========================

  
```scala

import java.util.{ UUID, Date }
import com.datastax.driver.core.Row
import com.newzly.phantom.sample.ExampleModel
import com.newzly.phantom.Implicits._

sealed class ExampleRecord private() extends CassandraTable[ExampleRecord, ExampleModel] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder with Ascending
  object name extends StringColumn(this)
  object props extends MapColumn[String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}


```

Querying with Phantom
=====================

The query syntax is inspired by the Foursquare Rogue library and aims to replicate CQL 3 as much as possible.

Phantom works with both Scala Futures and Twitter Futures. For the Twitter flavour, simply add the ```"com.newzly  %% phantom-finagle % 0.1.0"``` dependency. 

```scala

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"

  // now define a session, which is a Cluster connection.
  implicit val session = SomeCassandraClient.session;
  
  def getRecordsByName(name: String): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).fetch
  }
  
  def getOneRecordByName(name: String): Future[Option[ExampleModel]] = {
    ExampelRecord.select.where(_.name eqs name).one()
  }
  
  // preserving order in Cassandra is not the simplest thing, but:
  def getRecordPage(start: Int, limit: Int): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.skip(start).limit(10).fetch
  }
  
}
```


Large record sets
=================

Phantom comes packed with CQL rows asynchronous lazy iterators to help you deal with billions of records.

Usage is trivial:

```scala
ExampleRecord.select.fetchEnumerator.foreach {
   item => println(item.toString)
}
```

Batch statements
================

phantom also brrings in support for batch statements. To use them, see [IterateeBigTest.scala]( https://github.com/newzly/phantom/blob/develop/phantom-test/src/test/scala/com/newzly/phantom/iteratee/IterateeBigTest.scala)

We have tested with 10,000 statements per batch, and 1000 batches processed simulatenously.


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
Copyright 2013 WhiskLabs, Copyright 2013 - 2014 newzly ltd.


Contributions
=============

Contributions are most welcome! 

To contribute, simply submit a "Pull request" via GitHub.
