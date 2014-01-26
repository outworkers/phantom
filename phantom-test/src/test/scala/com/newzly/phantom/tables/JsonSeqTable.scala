package com.newzly.phantom.tables

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.keys.{ LongOrderKey, PrimaryKey }
import com.newzly.phantom.helper.TestSampler
import com.newzly.phantom.Implicits._

sealed class JsonSeqTable extends CassandraTable[JsonSeqTable, JsonSeqRow]
  with LongOrderKey[JsonSeqTable, JsonSeqRow] {
  override def fromRow(r: Row): JsonSeqRow = {
    JsonSeqRow(pkey(r), recipes(r))
  }

  object pkey extends StringColumn(this) with PrimaryKey[JsonSeqTable, JsonSeqRow]
  object recipes extends JsonSeqColumn[JsonSeqTable, JsonSeqRow, Recipe](this)
}

object JsonSeqTable extends JsonSeqTable with TestSampler[JsonSeqTable, JsonSeqRow] {
  override val tableName = "JsonSeqColumnTable"

  def createSchema: String =
    """|CREATE TABLE JsonSeqColumnTable(
      |pkey text PRIMARY KEY,
      |recipes seq<text>
      |);
    """.stripMargin
}