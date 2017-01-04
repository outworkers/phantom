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

import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

abstract class TupleTypeTable extends CassandraTable[TupleTypeTable, (UUID, String, String)] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object name extends StringColumn(this)
  object description extends StringColumn(this)

  def store(tp: (UUID, String, String)): InsertQuery.Default[TupleTypeTable, (UUID, String, String)] = {
    insert.value(_.id, tp._1)
      .value(_.name, tp._2)
      .value(_.description, tp._3)
  }

  def findById(id: UUID): Future[Option[(UUID, String, String)]] = {
    select.where(_.id eqs id).one()
  }
}
