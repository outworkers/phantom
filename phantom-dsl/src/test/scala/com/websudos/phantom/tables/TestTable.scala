/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.tables

import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector

case class TestRow(
  key: String,
  list: List[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String]
)

sealed class TestTable extends CassandraTable[TestTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey[String]

  object list extends ListColumn[TestTable, TestRow, String](this)

  object setText extends SetColumn[TestTable, TestRow, String](this)

  object mapTextToText extends MapColumn[TestTable, TestRow, String, String](this)

  object setInt extends SetColumn[TestTable, TestRow, Int](this)

  object mapIntToText extends MapColumn[TestTable, TestRow, Int, String](this)

  def fromRow(r: Row): TestRow = {
    TestRow(
      key(r),
      list(r),
      setText(r),
      mapTextToText(r),
      setInt(r).toSet,
      mapIntToText(r)
    )
  }
}

object TestTable extends TestTable with PhantomCassandraConnector {
  override val tableName = "TestTable"
}

