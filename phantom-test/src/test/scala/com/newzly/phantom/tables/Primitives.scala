package com.newzly.phantom.tables

import com.newzly.phantom.{PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.Row
import com.newzly.phantom.helper.{ModelSampler, Sampler, TestSampler}
import java.net.InetAddress
import java.util.{UUID, Date}

case class Primitive(
  pkey: String,
  long: Long,
  boolean: Boolean,
  bDecimal: BigDecimal,
  double: Double,
  float: Float,
  inet: java.net.InetAddress,
  int: Int,
  date: java.util.Date,
  uuid: java.util.UUID,
  bi: BigInt
)

object Primitive extends ModelSampler {
  def sample: Primitive = {
    Primitive(
      Sampler.getAUniqueString,
      Sampler.getARandomInteger().toLong,
      boolean = false,
      BigDecimal(Sampler.getARandomInteger()),
      Sampler.getARandomInteger().toDouble,
      Sampler.getARandomInteger().toFloat,
      new InetAddress,
      Sampler.getARandomInteger(),
      new Date(),
      UUID.randomUUID(),
      BigInt(Sampler.getARandomInteger())
    )
  }
}

sealed class Primitives extends CassandraTable[Primitives, Primitive] {
  override def fromRow(r: Row): Primitive = {
    Primitive(pkey(r), long(r), boolean(r), bDecimal(r), double(r), float(r), inet(r),
      int(r), date(r), uuid(r), bi(r))
  }

  object pkey extends PrimitiveColumn[String]

  object long extends PrimitiveColumn[Long]

  object boolean extends PrimitiveColumn[Boolean]

  object bDecimal extends PrimitiveColumn[BigDecimal]

  object double extends PrimitiveColumn[Double]

  object float extends PrimitiveColumn[Float]

  object inet extends PrimitiveColumn[java.net.InetAddress]

  object int extends PrimitiveColumn[Int]

  object date extends PrimitiveColumn[java.util.Date]

  object uuid extends PrimitiveColumn[java.util.UUID]

  object bi extends PrimitiveColumn[BigInt]

  val _key = pkey
}

object Primitives extends Primitives with TestSampler[Primitive] {
  override val tableName = "Primitives"

  def createSchema: String = ""

}