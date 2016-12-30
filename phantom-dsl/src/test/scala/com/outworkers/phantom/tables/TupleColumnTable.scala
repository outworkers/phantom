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

import java.util.UUID

import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class TupleRecord(id: UUID, tp: (String, Long))

class TupleColumnTable extends CassandraTable[ConcreteTupleColumnTable, TupleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object tp extends TupleColumn[(String, Long)](this)
}

abstract class ConcreteTupleColumnTable extends TupleColumnTable with RootConnector {

  def store(rec: TupleRecord): InsertQuery.Default[ConcreteTupleColumnTable, TupleRecord] = {
    insert
      .value(_.id, rec.id)
      .value(_.tp, rec.tp)
  }

  def findById(id: UUID): Future[Option[TupleRecord]] = {
    select.where(_.id eqs id).one()
  }
}

case class NestedTupleRecord(id: UUID, tp: (String, (String, Long)))

class NestedTupleColumnTable extends CassandraTable[ConcreteNestedTupleColumnTable, NestedTupleRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object tp extends TupleColumn[(String, (String, Long))](this)
}

abstract class ConcreteNestedTupleColumnTable extends NestedTupleColumnTable with RootConnector {

  def store(rec: NestedTupleRecord): InsertQuery.Default[ConcreteNestedTupleColumnTable, NestedTupleRecord] = {
    insert
      .value(_.id, rec.id)
      .value(_.tp, rec.tp)
  }

  def findById(id: UUID): Future[Option[NestedTupleRecord]] = {
    select.where(_.id eqs id).one()
  }
}


case class TupleCollectionRecord(id: UUID, tuples: List[(Int, String)])

class TupleCollectionsTable extends CassandraTable[ConcreteTupleCollectionsTable, TupleCollectionRecord] {
  object id extends UUIDColumn(this) with PartitionKey
  object tuples extends ListColumn[(Int, String)](this)
}

abstract class ConcreteTupleCollectionsTable extends TupleCollectionsTable with RootConnector {

  def store(rec: TupleCollectionRecord): InsertQuery.Default[ConcreteTupleCollectionsTable, TupleCollectionRecord] = {
    insert
      .value(_.id, rec.id)
      .value(_.tuples, rec.tuples)
  }

  def findById(id: UUID): Future[Option[TupleCollectionRecord]] = {
    select.where(_.id eqs id).one()
  }
}
