phantom
==============
Asynchronous Scala DSL for Cassandra

[![Build Status](https://magnum.travis-ci.com/newzly/phantom.png?token=tyRTmBk14WrDycpepg9c&branch=master)](https://magnum.travis-ci.com/newzly/phantom)


Data modeling with phantom
==========================

  
```scala

import java.util.{ UUID, Date }
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.phantom.field.{ TimeUUIDPk, LongOrderKey }
import com.newzly.phantom.Implicits._

case class ExampleModel(val id: UUID, val name: String, val props: Map[String, String]);

sealed class ExampleRecord private() extends CassandraTable[ExampleRecord, ExampleModel] with TimeUUIDPk[ExampleRecord] with LongOrderKey[ExampleRecord] {

  object name extends PrimitiveColumn[String]
  object props extends MapColumn[String, String]

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row));
  }
}


```

Querying with Phantom
=====================

The query syntax is modelled around the Foursquare Rogue library.
The syntax provided aims to replicate CQL 3 as much as possible.

```scala

import scala.concurrent.ExecutionContext.Implicits.global

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

Phantom was originally developed at newzly as an in-house project destined for internal-only use.
All Cassandra integration at newzly goes through Phantom.

- Sorin Chiprian sorin.chiprian@newzly.com
- Flavian Alexandru flavian@newzly.com

Contributions are most welcome!
