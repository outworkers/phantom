package com.newzly.phantom.tables

import org.joda.time.DateTime
import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.Row
import com.newzly.phantom.helper.{TestSampler, Sampler, ModelSampler}


case class JodaRow( pkey: String, int: Int,
  bi: DateTime)

object JodaRow extends ModelSampler {
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
  object pkey extends PrimitiveColumn[String]
  object int extends PrimitiveColumn[Int]
  object bi extends PrimitiveColumn[DateTime]
  val _key = pkey
}

object PrimitivesJoda extends PrimitivesJoda with TestSampler[JodaRow] {

  override val tableName = "PrimitivesJoda"

  def createSchema = ""
}

