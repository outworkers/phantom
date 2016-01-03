package com.websudos.phantom.tables

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.column.{ListColumn, MapColumn, SetColumn}
import com.websudos.phantom.dsl._

sealed class IndexedCollectionsTable extends CassandraTable[ConcreteIndexedCollectionsTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey[String]

  object list extends ListColumn[ConcreteIndexedCollectionsTable, TestRow, String](this)

  object setText extends SetColumn[ConcreteIndexedCollectionsTable, TestRow, String](this) with Index[Set[String]]

  object mapTextToText extends MapColumn[ConcreteIndexedCollectionsTable, TestRow, String, String](this) with Index[Map[String, String]]

  object setInt extends SetColumn[ConcreteIndexedCollectionsTable, TestRow, Int](this)

  object mapIntToText extends MapColumn[ConcreteIndexedCollectionsTable, TestRow, Int, String](this) with Index[Map[Int, String]] with Keys

  def fromRow(r: Row): TestRow = {
    TestRow(
      key = key(r),
      list = list(r),
      setText = setText(r),
      mapTextToText = mapTextToText(r),
      setInt = setInt(r),
      mapIntToText = mapIntToText(r)
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
  }

}


