package com.websudos.phantom.tables

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.column.{ListColumn, MapColumn, SetColumn}
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._

sealed class IndexedCollectionsTable extends CassandraTable[IndexedCollectionsTable, TestRow] {

  object key extends StringColumn(this) with PartitionKey[String]

  object list extends ListColumn[IndexedCollectionsTable, TestRow, String](this)

  object setText extends SetColumn[IndexedCollectionsTable, TestRow, String](this) with Index[Set[String]]

  object mapTextToText extends MapColumn[IndexedCollectionsTable, TestRow, String, String](this) with Index[Map[String, String]]

  object setInt extends SetColumn[IndexedCollectionsTable, TestRow, Int](this)

  object mapIntToText extends MapColumn[IndexedCollectionsTable, TestRow, Int, String](this)

  def fromRow(r: Row): TestRow = {
    TestRow(
      key(r),
      list(r),
      setText(r),
      mapTextToText(r),
      setInt(r),
      mapIntToText(r)
    )
  }
}

object IndexedCollectionsTable extends IndexedCollectionsTable with PhantomCassandraConnector {
  override val tableName = "indexed_collections"

  def store(row: TestRow): InsertQuery.Default[IndexedCollectionsTable, TestRow] = {
    insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
  }

}


