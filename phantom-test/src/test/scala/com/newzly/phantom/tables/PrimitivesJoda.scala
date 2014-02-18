package com.newzly.phantom.tables

import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{TestSampler, Sampler, ModelSampler}
import com.newzly.phantom.Implicits._
import com.newzly.phantom.keys.{PartitionKey, PrimaryKey}

case class JodaRow(
  pkey: String,
  int: Int,
  bi: DateTime
)

object JodaRow extends ModelSampler[JodaRow] {
  def sample: JodaRow = {
    val d = new DateTime()
    JodaRow(
      Sampler.getAUniqueString,
      Sampler.getARandomInteger(),
      new DateTime(d.plus(Sampler.getARandomInteger().toLong))
    )
  }
}

sealed class PrimitivesJoda extends CassandraTable[PrimitivesJoda, JodaRow] {
  override def fromRow(r: Row): JodaRow = {
    JodaRow(pkey(r), intColumn(r), timestamp(r))
  }

  object pkey extends StringColumn(this) with PartitionKey[String]
  object intColumn extends IntColumn(this)
  object timestamp extends DateTimeColumn(this)
}

object PrimitivesJoda extends PrimitivesJoda with TestSampler[PrimitivesJoda, JodaRow] {

  override val tableName = "PrimitivesJoda"

}

