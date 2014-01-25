package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import java.net.InetAddress
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._

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

object Primitive extends ModelSampler[Primitive] {
  def sample: Primitive = {
    Primitive(
      Sampler.getAUniqueString,
      Sampler.getARandomInteger().toLong,
      boolean = false,
      BigDecimal(Sampler.getARandomInteger()),
      Sampler.getARandomInteger().toDouble,
      Sampler.getARandomInteger().toFloat,
      InetAddress.getByName("127.0.0.1"),
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

  object pkey extends PrimitiveColumn[Primitives, Primitive, String](this)

  object long extends PrimitiveColumn[Primitives, Primitive, Long](this)

  object boolean extends PrimitiveColumn[Primitives, Primitive, Boolean](this)

  object bDecimal extends PrimitiveColumn[Primitives, Primitive, BigDecimal](this)

  object double extends PrimitiveColumn[Primitives, Primitive, Double](this)

  object float extends PrimitiveColumn[Primitives, Primitive, Float](this)

  object inet extends PrimitiveColumn[Primitives, Primitive, InetAddress](this)

  object int extends PrimitiveColumn[Primitives, Primitive, Int](this)

  object date extends PrimitiveColumn[Primitives, Primitive, Date](this)

  object uuid extends PrimitiveColumn[Primitives, Primitive, UUID](this)

  object bi extends PrimitiveColumn[Primitives, Primitive, BigInt](this)

  val _key = pkey
}

object Primitives extends Primitives with TestSampler[Primitives, Primitive] {
  override val tableName = "Primitives"

  def createSchema: String = ""

}