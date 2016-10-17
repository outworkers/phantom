/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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



