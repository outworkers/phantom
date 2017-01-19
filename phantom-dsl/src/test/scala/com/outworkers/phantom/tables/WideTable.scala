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

case class WideRow(
  id: UUID,
  field: String,
  field1: String,
  field2: String,
  field3: String,
  field4: String,
  field5: String,
  field6: String,
  field7: String,
  field8: String,
  field9: String,
  field10: String,
  field11: String,
  field12: String,
  field13: String,
  field14: String,
  field15: String,
  field16: String,
  field17: String,
  field18: String,
  field19: String,
  field20: String
)

abstract class WideTable extends CassandraTable[WideTable, WideRow] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object field extends StringColumn(this)
  object field1 extends StringColumn(this)
  object field2 extends StringColumn(this)
  object field3 extends StringColumn(this)
  object field4 extends StringColumn(this)
  object field5 extends StringColumn(this)
  object field6 extends StringColumn(this)
  object field7 extends StringColumn(this)
  object field8 extends StringColumn(this)
  object field9 extends StringColumn(this)
  object field10 extends StringColumn(this)
  object field11 extends StringColumn(this)
  object field12 extends StringColumn(this)
  object field13 extends StringColumn(this)
  object field14 extends StringColumn(this)
  object field15 extends StringColumn(this)
  object field16 extends StringColumn(this)
  object field17 extends StringColumn(this)
  object field18 extends StringColumn(this)
  object field19 extends StringColumn(this)
  object field20 extends StringColumn(this)

  def store(row: WideRow): InsertQuery.Default[WideTable, WideRow] = {
    insert.value(_.id, row.id)
      .value(_.field, row.field)
      .value(_.field1, row.field1)
      .value(_.field2, row.field2)
      .value(_.field3, row.field3)
      .value(_.field4, row.field4)
      .value(_.field5, row.field5)
      .value(_.field6, row.field6)
      .value(_.field7, row.field7)
      .value(_.field8, row.field8)
      .value(_.field9, row.field9)
      .value(_.field10, row.field10)
      .value(_.field11, row.field11)
      .value(_.field12, row.field12)
      .value(_.field13, row.field13)
      .value(_.field14, row.field14)
      .value(_.field15, row.field15)
      .value(_.field16, row.field16)
      .value(_.field17, row.field17)
      .value(_.field18, row.field18)
      .value(_.field19, row.field19)
      .value(_.field20, row.field20)
  }
}


