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

case class MyTestRow(
  key: String,
  optionA: Option[Int],
  stringlist: List[String]
)

abstract class ListCollectionTable extends CassandraTable[ListCollectionTable, MyTestRow] with RootConnector {

  object key extends StringColumn(this) with PartitionKey

  object optionA extends OptionalIntColumn(this)

  object stringlist extends ListColumn[String](this)

  def store(row: MyTestRow): InsertQuery.Default[ListCollectionTable, MyTestRow] = {
    insert().value(_.key, row.key)
      .value(_.stringlist, row.stringlist)
      .value(_.optionA, row.optionA)
  }

}


