/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.tables

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.macros.TableHelper
import org.joda.time.DateTime
import org.scalamock.scalatest.MockFactory

case class Ev2(
  id: UUID,
  set: Set[String]
)

abstract class Events2 extends Table[Events2, Ev2] {
  object partition extends UUIDColumn with PartitionKey
  object id extends UUIDColumn with PartitionKey
  object set extends SetColumn[String]
}

case class ClusteredRecord(
  partition: UUID,
  id: UUID,
  id2: UUID,
  id3: UUID
)

abstract class ClusteredTable extends Table[ClusteredTable, ClusteredRecord] {
  object partition extends UUIDColumn with PartitionKey
  object id extends UUIDColumn with ClusteringOrder with Descending
  object id2 extends UUIDColumn with ClusteringOrder with Descending
  object id3 extends UUIDColumn with ClusteringOrder with Ascending
}

class TableHelperTest extends PhantomSuite with MockFactory {

  it should "not generate a fromRow if a normal type is different" in {

    case class SampleEvent(id: String, map: Map[Long, DateTime])

    abstract class Events extends Table[Events, SampleEvent] {
      object id extends UUIDColumn with PartitionKey
      object map extends MapColumn[Long, Long]
    }

    val ev = new Events() with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the columns match but are in the wrong order" in {

    case class Event(
      id: UUID,
      text: String,
      length: Int,
      map: Map[Long, DateTime]
    )

    abstract class Events extends Table[Events, Event] {
      object id extends UUIDColumn with PartitionKey
      object text extends StringColumn
      object map extends MapColumn[Long, Long]
      object length extends IntColumn
    }

    val ev = new Events() with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a map column is different" in {

    case class SampleEvent(id: UUID, map: Map[Long, DateTime])

    abstract class Events extends Table[Events, SampleEvent] {
      object id extends UUIDColumn with PartitionKey
      object map extends MapColumn[Long, Long]
    }

    val ev = new Events() with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if  the collection type is different" in {
    case class Ev(id: UUID, set: List[Int])

    abstract class Events extends Table[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends SetColumn[Int]
    }

    val ev = new Events( )with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a list column is different" in {
    case class Ev(id: UUID, set: List[Int])

    abstract class Events extends Table[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends ListColumn[String]
    }

    val ev = new Events() with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a set column is different" in {
    case class Ev(id: UUID, set: Set[Int])

    abstract class Events extends Table[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends SetColumn[String]
    }

    val ev = new Events() with database.Connector
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "correctly retrieve a list of keys" in {
    val table = new ClusteredTable with database.Connector
    val fields = TableHelper[ClusteredTable, ClusteredRecord].fields(table)
    fields should contain theSameElementsInOrderAs Seq(table.partition, table.id, table.id2, table.id3)
  }

  it should "retrieve clustering keys in the order they are written" in {
    val table = new ClusteredTable with database.Connector
    table.clusteringColumns should contain theSameElementsInOrderAs Seq(table.id, table.id2, table.id3)
  }


  it should "generate a fromRow method from a partial table definition" in {
    val row = stub[Row]
    val ev = new Events2() with database.Connector

    ev.fromRow(row)
  }
}
