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

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.column.{ListColumn, MapColumn, SetColumn}
import com.outworkers.phantom.dsl._

sealed class IndexedCollectionsTable extends CassandraTable[ConcreteIndexedCollectionsTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey[String]

  object list extends ListColumn[String](this)

  object setText extends SetColumn[String](this) with Index[Set[String]]

  object mapTextToText extends MapColumn[String, String](this) with Index[Map[String, String]]

  object setInt extends SetColumn[Int](this)

  object mapIntToText extends MapColumn[Int, String](this) with Index[Map[Int, String]] with Keys

  object mapIntToInt extends MapColumn[Int, Int](this)

  def fromRow(r: Row): TestRow = {
    TestRow(
      key = key(r),
      list = list(r),
      setText = setText(r),
      mapTextToText = mapTextToText(r),
      setInt = setInt(r),
      mapIntToText = mapIntToText(r),
      mapIntToInt = mapIntToInt(r)
    )
  }
}

abstract class ConcreteIndexedCollectionsTable extends IndexedCollectionsTable with RootConnector {
  override val tableName = "indexed_collections"

  def store(row: TestRow): InsertQuery.Default[ConcreteIndexedCollectionsTable, TestRow] = {
    insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .value(_.mapIntToInt, row.mapIntToInt)
  }

}


sealed class IndexedEntriesTable extends CassandraTable[ConcreteIndexedEntriesTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey[String]

  object list extends ListColumn[String](this)

  object setText extends SetColumn[String](this) with Index[Set[String]]

  object mapTextToText extends MapColumn[String, String](this) with Index[Map[String, String]]

  object setInt extends SetColumn[Int](this)

  object mapIntToText extends MapColumn[Int, String](this) with Index[Map[Int, String]] with Keys

  object mapIntToInt extends MapColumn[Int, Int](this) with Index[Map[Int, Int]] with Entries

  def fromRow(r: Row): TestRow = {
    TestRow(
      key = key(r),
      list = list(r),
      setText = setText(r),
      mapTextToText = mapTextToText(r),
      setInt = setInt(r),
      mapIntToText = mapIntToText(r),
      mapIntToInt = mapIntToInt(r)
    )
  }
}

abstract class ConcreteIndexedEntriesTable extends IndexedEntriesTable with RootConnector {
  override val tableName = "indexed_collections"

  def store(row: TestRow): InsertQuery.Default[ConcreteIndexedEntriesTable, TestRow] = {
    insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .value(_.mapIntToInt, row.mapIntToInt)
  }

}



