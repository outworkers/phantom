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

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class PrimaryCollectionRecord(
  index: List[String],
  set: Set[String],
  map: Map[String, String],
  name: String,
  value: Int
)

class PrimaryCollectionTable extends CassandraTable[ConcretePrimaryCollectionTable, PrimaryCollectionRecord] {
  object listIndex extends ListColumn[String](this) with PartitionKey[List[String]]
  object setCol extends SetColumn[String](this) with PrimaryKey[Set[String]]
  object mapCol extends MapColumn[String, String](this) with PrimaryKey[Map[String, String]]
  object name extends StringColumn(this) with PrimaryKey[String]
  object value extends IntColumn(this)

  def fromRow(row: Row): PrimaryCollectionRecord = {
    PrimaryCollectionRecord(
      listIndex(row),
      setCol(row),
      mapCol(row),
      name(row),
      value(row)
    )
  }
}

abstract class ConcretePrimaryCollectionTable extends PrimaryCollectionTable with RootConnector {

  def store(rec: PrimaryCollectionRecord): Future[ResultSet] = {
    insert.value(_.listIndex, rec.index)
      .value(_.setCol, rec.set)
      .value(_.mapCol, rec.map)
      .value(_.name, rec.name)
      .value(_.value, rec.value)
      .future()
  }
}