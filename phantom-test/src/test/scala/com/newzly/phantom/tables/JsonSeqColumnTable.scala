package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, JsonSeqColumn, PrimitiveColumn }
import com.newzly.phantom.field.{ LongOrderKey, UUIDPk }
import com.newzly.phantom.helper.TestSampler

sealed class JsonSeqColumnTable extends CassandraTable[JsonSeqColumnTable, JsonSeqColumnRow] with UUIDPk[JsonSeqColumnTable]
with LongOrderKey[JsonSeqColumnTable] {
  override def fromRow(r: Row): JsonSeqColumnRow = {
    JsonSeqColumnRow(pkey(r), recipes(r))
  }
  object pkey extends PrimitiveColumn[String]
  object recipes extends JsonSeqColumn[Recipe]
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