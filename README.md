phantom
==============
Asynchronous Scala DSL for Cassandra

[![Build Status](https://magnum.travis-ci.com/newzly/phantom.png?token=tyRTmBk14WrDycpepg9c&branch=master)](https://magnum.travis-ci.com/newzly/phantom)

Thrift IDL definitions
======================
```thrift
namespace java com.newzly.phantom.sample.ExampleModel

stuct Model {
  1: required i32 id,
  2: required string name,
  3: required Map<string, string> props,
  4: required i32 timestamp
  5: optional i32 test
}
```

Data modeling with phantom
==========================

  
```scala

import java.util.{ UUID, Date }
import com.datastax.driver.core.Row
import com.newzly.phantom.sample.ExampleModel
import com.newzly.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.phantom.keys.{ TimeUUIDPk, LongOrderKey }
import com.newzly.phantom.Implicits._

sealed class ExampleRecord private() extends CassandraTable[ExampleRecord, ExampleModel] {

  object id extends UUIDColumn(this) with PrimaryKey[ExampleRecord, ExampleModel]
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[ExampleRecord, ExampleModel] with Ascending
  object name extends PrimitiveColumn[String](this)
  object props extends MapColumn[String, String](this)
  object test extends OptionalIntColumn(this)

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row), timestamp(row), test(row));
  }
}


```

Querying with Phantom
=====================

The query syntax is modelled around the Foursquare Rogue library.
The syntax provided aims to replicate CQL 3 as much as possible.

```scala

import com.twitter.util.Future

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"

  // now define a session, which is a Cluster connection.
  implicit val session = SomeCassandraClient.session;
  
  def getRecordsByName(name: String): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.where(_.name eqs name).execute()
  }
  
  def getOneRecordByName(name: String): Future[Option[ExampleModel]] = {
    ExampelRecord.select.where(_.name eqs name).one()
  }
  
  // preserving order in Cassandra is not the simplest thing, but:
  def getRecordPage(start: Int, limit: Int): Future[Seq[ExampleModel]] = {
    ExampleRecord.select.skip(start).limit(10).execute()
  }
  
}
```


Maintainers
===========

Phantom was originally developed at newzly as an in-house project.
All Cassandra integration at newzly goes through Phantom.

- Sorin Chiprian sorin.chiprian@newzly.com
- Krisztian Kovacs krisztian.kovacs@newzly.com
- Flavian Alexandru flavian@newzly.com

Pre newzly fork
===============
Special thanks to Viktor Taranenko from WhiskLabs, who gave us the original idea.

Contributions
=============

Contributions are most welcome! 

To contribute, simply submit a "Pull request" via GitHub.
