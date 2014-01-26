package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import java.net.InetAddress
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.keys.PrimaryKey

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

  def meta = Primitives

  object pkey extends StringColumn(this) with PrimaryKey[Primitives, Primitive]

  object long extends LongColumn(this)

  object boolean extends BooleanColumn(this)

  object bDecimal extends BigDecimalColumn(this)

  object double extends DoubleColumn(this)

  object float extends FloatColumn(this)

  object inet extends InetAddressColumn(this)

  object int extends IntColumn(this)

  object date extends DateColumn(this)

  object uuid extends UUIDColumn(this)

  object bi extends BigIntColumn(this)
}

object Primitives extends Primitives with TestSampler[Primitives, Primitive] {
  override val tableName = "Primitives"

  def createSchema: String = ""

}