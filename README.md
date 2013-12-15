newzly phantom
==============
Asynchronous Scala DSL for Cassandra

[![Build Status](https://magnum.travis-ci.com/newzly/phantom.png?token=tyRTmBk14WrDycpepg9c&branch=master)](https://magnum.travis-ci.com/newzly/phantom)



1. Creating model definitions with Phantom Record.
  
```scala
import java.util.{ UUID, Date }
import com.datastax.driver.core.Row
import com.newzly.cassandra.phantom.{ CassandraTable, PrimitiveColumn }
import com.newzly.cassandra.phantom.Implicits._

case class ExampleModel(val id: UUID, val name: String, val props: Map[String, String]);

sealed class ExampleRecord private() extends CassandraTable[ExampleRecord, ExampleModel] {

  object id extends PrimitiveColumn[UUID]
  object name extends PrimitiveColumn[String]
  object props extends MapColumn[String, String]

  override def fromRow(row: Row): ExampleModel = {
    ExampleModel(id(row), name(row), props(row));
  }
}

object ExampleRecord extends ExampleRecord {
  override val tableName = "examplerecord"
}
```
