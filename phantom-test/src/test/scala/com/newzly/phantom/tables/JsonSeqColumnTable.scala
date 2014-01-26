package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.keys.{ LongOrderKey, UUIDPk }
import com.newzly.phantom.helper.TestSampler
import com.newzly.phantom.Implicits._

sealed class JsonSeqColumnTable extends CassandraTable[JsonSeqColumnTable, JsonSeqColumnRow] with UUIDPk[JsonSeqColumnTable, JsonSeqColumnRow]
  with LongOrderKey[JsonSeqColumnTable, JsonSeqColumnRow] {
  override def fromRow(r: Row): JsonSeqColumnRow = {
    JsonSeqColumnRow(pkey(r), recipes(r))
  }
  object pkey extends PrimitiveColumn[JsonSeqColumnTable, JsonSeqColumnRow, String](this)
  object recipes extends JsonSeqColumn[JsonSeqColumnTable, JsonSeqColumnRow, Recipe](this)
}

object JsonSeqColumnTable extends JsonSeqColumnTable with TestSampler[JsonSeqColumnTable, JsonSeqColumnRow] {
  override val tableName = "JsonSeqColumnTable"

  def createSchema: String =
    """|CREATE TABLE JsonSeqColumnTable(
      |pkey text PRIMARY KEY,
      |recipes seq<text>
      |);
    """.stripMargin
}