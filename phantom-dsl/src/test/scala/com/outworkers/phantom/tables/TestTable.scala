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
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

case class TestRow(
  key: String,
  list: List[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String],
  mapIntToInt: Map[Int, Int]
)

sealed class TestTable extends CassandraTable[ConcreteTestTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey

  object list extends ListColumn[String](this)

  object setText extends SetColumn[String](this)

  object mapTextToText extends MapColumn[String, String](this)

  object setInt extends SetColumn[Int](this)

  object mapIntToText extends MapColumn[Int, String](this)
}

abstract class ConcreteTestTable extends TestTable with RootConnector {
  override val tableName = "TestTable"

  def store(row: TestRow): InsertQuery.Default[ConcreteTestTable, TestRow] = {
    insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
  }

}

