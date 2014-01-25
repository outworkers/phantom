package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraTable, JsonSeqColumn, PrimitiveColumn }
import com.newzly.phantom.field.{ LongOrderKey, UUIDPk }
import com.newzly.phantom.helper.TestSampler

sealed class JsonTypeSeqColumnTable extends CassandraTable[JsonTypeSeqColumnTable, JsonSeqColumnRow] with UUIDPk[JsonTypeSeqColumnTable]
with LongOrderKey[JsonTypeSeqColumnTable] {
  override def fromRow(r: Row): JsonSeqColumnRow = {
    JsonSeqColumnRow(pkey(r), jtsc(r))
  }
  object pkey extends PrimitiveColumn[String]
  object jtsc extends JsonSeqColumn[Recipe]
}

object JsonTypeSeqColumnTable extends JsonTypeSeqColumnTable with TestSampler {
  def createSchema: String = ""

}