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
package com.outworkers.phantom.macros

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.primitives.Primitive
import org.joda.time.DateTime
import com.outworkers.phantom.dsl._
import org.scalamock.scalatest.MockFactory
import com.outworkers.util.samplers._

import scala.collection.JavaConverters._

case class Ev2(
  id: UUID,
  set: Set[String]
)

class Events2 extends CassandraTable[Events2, Ev2] {
  object partition extends UUIDColumn with PartitionKey
  object id extends UUIDColumn with PartitionKey
  object map extends SetColumn[String]
}

case class ClusteredRecord(
  partition: UUID,
  id: UUID,
  id2: UUID,
  id3: UUID
)

class ClusteredTable extends CassandraTable[ClusteredTable, ClusteredRecord] {
  object partition extends UUIDColumn with PartitionKey
  object id extends UUIDColumn with ClusteringOrder with Descending
  object id2 extends UUIDColumn with ClusteringOrder with Descending
  object id3 extends UUIDColumn with ClusteringOrder with Ascending
}

class TableHelperTest extends PhantomSuite with MockFactory {

  it should "not generate a fromRow if a normal type is different" in {

    case class SampleEvent(id: String, map: Map[Long, DateTime])

    class Events extends CassandraTable[Events, SampleEvent] {
      object id extends UUIDColumn with PartitionKey
      object map extends MapColumn[Long, Long]
    }

    val ev = new Events()
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

    class Events extends CassandraTable[Events, Event] {
      object id extends UUIDColumn with PartitionKey
      object text extends StringColumn
      object map extends MapColumn[Long, Long]
      object length extends IntColumn
    }

    val ev = new Events()
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a map column is different" in {

    case class SampleEvent(id: UUID, map: Map[Long, DateTime])

    class Events extends CassandraTable[Events, SampleEvent] {
      object id extends UUIDColumn with PartitionKey
      object map extends MapColumn[Long, Long]
    }

    val ev = new Events()
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if  the collection type is different" in {
    case class Ev(id: UUID, set: List[Int])

    class Events extends CassandraTable[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends SetColumn[Int]
    }

    val ev = new Events()
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a list column is different" in {
    case class Ev(id: UUID, set: List[Int])

    class Events extends CassandraTable[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends ListColumn[String]
    }

    val ev = new Events()
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "not generate a fromRow if the argument passed to a set column is different" in {
    case class Ev(id: UUID, set: Set[Int])

    class Events extends CassandraTable[Events, Ev] {
      object id extends UUIDColumn with PartitionKey
      object map extends SetColumn[String]
    }

    val ev = new Events()
    intercept[NotImplementedError] {
      ev.fromRow(null.asInstanceOf[Row])
    }
  }

  it should "correctly retrieve a list of keys" in {
    val table = new ClusteredTable
    val fields = TableHelper[ClusteredTable, ClusteredRecord].fields(table)
    fields shouldEqual Seq(table.partition, table.id, table.id2, table.id3)
  }

  it should "generate a fromRow method from a partial table definition" in {
    val row = stub[Row]
    val ev = new Events2()

    intercept[NullPointerException] {
      ev.fromRow(row)
    }
  }
}
