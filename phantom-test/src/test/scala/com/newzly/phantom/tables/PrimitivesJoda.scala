package com.newzly.phantom.tables

import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{TestSampler, Sampler, ModelSampler}
import com.newzly.phantom.Implicits._

case class JodaRow( pkey: String, int: Int,
  bi: DateTime)

object JodaRow extends ModelSampler[JodaRow] {
  def sample: JodaRow = {
    JodaRow(
      Sampler.getAUniqueString,
      Sampler.getARandomInteger(),
      new DateTime()
    )
  }
}

sealed class PrimitivesJoda extends CassandraTable[PrimitivesJoda, JodaRow] {
  override def fromRow(r: Row): JodaRow = {
    JodaRow(pkey(r),int(r),  bi(r))
  }
  object pkey extends PrimitiveColumn[PrimitivesJoda, JodaRow, String](this)
  object int extends PrimitiveColumn[PrimitivesJoda, JodaRow, Int](this)
  object bi extends PrimitiveColumn[PrimitivesJoda, JodaRow, DateTime](this)
  val _key = pkey
}

object PrimitivesJoda extends PrimitivesJoda with TestSampler[PrimitivesJoda, JodaRow] {

  override val tableName = "PrimitivesJoda"

  def createSchema = ""
}

