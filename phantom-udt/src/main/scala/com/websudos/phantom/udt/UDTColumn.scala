package com.websudos.phantom.udt

import java.net.InetAddress
import java.util.{Date, UUID}

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.DynamicVariable

import org.joda.time.DateTime

import com.datastax.driver.core.{Row, UDTValue, UserType}
import com.websudos.phantom.column.Column
import com.websudos.phantom.zookeeper.CassandraConnector
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}


/**
 * A global lock for reflecting and collecting fields inside a User Defined Type.
 * This prevents a race condition and bug.
 */
private[phantom] object Lock

/**
 * A field part of a user defined type.
 * @param owner The UDT column that owns the field.
 * @tparam T The Scala type corresponding the underlying Cassandra type of the UDT field.
*/
sealed abstract class AbstractField[@specialized(Int, Double, Float, Long, Boolean, Short) T : CassandraPrimitive](owner: UDTColumn[_, _, _]) {

  type ValueType = T

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")

  protected[udt] lazy val valueBox = new DynamicVariable[Option[T]](None)

  def value: T = valueBox.value.getOrElse(null.asInstanceOf[T])

  private[udt] def setSerialise(data: UDTValue): UDTValue

  private[udt] def set(value: Option[T]): Unit = valueBox.value_=(value)

  private[udt] def set(data: UDTValue): Unit = valueBox.value_=(apply(data))

  def cassandraType: String = CassandraPrimitive[T].cassandraType

  def apply(row: UDTValue): Option[T]
}


sealed abstract class Field[
  Owner <: CassandraTable[Owner, Record],
  Record,
  FieldOwner <: UDTColumn[Owner, Record, _],
  T : CassandraPrimitive
](column: FieldOwner) extends AbstractField[T](column) {}

object PrimitiveBoxedManifests {
  val StringManifest = manifest[String]
  val IntManifest = manifest[Int]
  val DoubleManifest = manifest[Double]
  val LongManifest = manifest[Long]
  val FloatManifest = manifest[Float]
  val BigDecimalManifest = manifest[BigDecimal]
  val BigIntManifest = manifest[BigInt]
}


abstract class UDTColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  T <: UDTColumn[Owner, Record, T]
](table: CassandraTable[Owner, Record]) extends Column[Owner,Record, T](table) {

  /**
   * Much like the definition of a Cassandra table where the columns are collected, the fields of an UDT are collected inside this buffer.
   * Every new buffer spawned will be a perfect clone of this instance, and the fields will always be pre-initialised on extraction.
   */
  private[udt] lazy val _fields: MutableArrayBuffer[AbstractField[_]] = new MutableArrayBuffer[AbstractField[_]] with MutableSyncBuffer[AbstractField[_]]

  def fields: List[AbstractField[_]] = _fields.toList

  def connector: CassandraConnector

  private[this] lazy val typeDef: UserType = connector.manager.cluster.getMetadata.getKeyspace(connector.keySpace).getUserType(name)

  override def apply(row: Row): T = {
    val instance: T = this.clone().asInstanceOf[T]
    val data = row.getUDTValue(this.name)

    instance.fields.foreach(field => {
      field.set(data)
    })

    instance
  }

  override def optional(r: Row): Option[T] = {
    val instance: T = this.clone().asInstanceOf[T]
    val data = r.getUDTValue(this.name)
    Some(instance)
  }

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  def toCType(v: T): AnyRef = {
    val data = typeDef.newValue()
    fields.foreach(field => {
      typeDef.asCQLQuery()
      field.setSerialise(data)
    })
    data
  }

  val cassandraType = _name.toLowerCase

  private[this] val instanceMirror = cm.reflect(this)
  private[this] val selfType = instanceMirror.symbol.toType

  // Collect all column definitions starting from base class
  private[this] val columnMembers = MutableArrayBuffer.empty[Symbol]

  Lock.synchronized {
    selfType.baseClasses.reverse.foreach {
      baseClass =>
        val baseClassMembers = baseClass.typeSignature.members.sorted
        val baseClassColumns = baseClassMembers.filter(_.typeSignature <:< ru.typeOf[AbstractField[_]])
        baseClassColumns.foreach(symbol => if (!columnMembers.contains(symbol)) columnMembers += symbol)
    }

    columnMembers.foreach {
      symbol =>
        val column = instanceMirror.reflectModule(symbol.asModule).instance
        _fields += column.asInstanceOf[AbstractField[_]]
    }
  }

  def schema(): String = {
    val queryInit = s"CREATE TYPE IF NOT EXISTS $cassandraType ("
    val queryColumns = _fields.foldLeft("")((qb, c) => {
        s"$qb, ${c.name} ${c.cassandraType}"
    })
    queryInit + queryColumns + """");""""
  }
}

class StringField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, String](column) {
  def apply(row: UDTValue): Option[String] = Option(row.getString(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setString(name, value)
}

class InetField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, InetAddress](column) {
  def apply(row: UDTValue): Option[InetAddress] = Option(row.getInet(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInet(name, value)
}

class IntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Int](column) {
  def apply(row: UDTValue): Option[Int] = Option(row.getInt(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setInt(name, value)
}

class DoubleField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Double](column) {
  def apply(row: UDTValue): Option[Double] = Option(row.getDouble(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDouble(name, value)
}

class LongField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, Long](column) {
  def apply(row: UDTValue): Option[Long] = Option(row.getLong(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setLong(name, value)
}

class BigIntField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigInt](column) {
  def apply(row: UDTValue): Option[BigInt] = Option(row.getVarint(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setVarint(name, value.bigInteger)
}

class BigDecimalField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T)  extends Field[Owner, Record, T, BigDecimal](column) {
  def apply(row: UDTValue): Option[BigDecimal] = Option(row.getDecimal(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDecimal(name, value.bigDecimal)
}

class DateField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, Date](column) {
  def apply(row: UDTValue): Option[Date] = Option(row.getDate(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value)
}

class DateTimeField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, DateTime](column) {
  def apply(row: UDTValue): Option[DateTime] = Option(new DateTime(row.getDate(name)))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setDate(name, value.toDate)
}

/*
class UDTField[Owner <: UDTColumn[Owner, ], T <: UDTColumn[_]](column: Owner) extends Field[Owner, T](column) {
  def apply(row: Row): DateTime = new DateTime(row.getDate(name))
}*/

class UUIDField[Owner <: CassandraTable[Owner, Record], Record, T <: UDTColumn[Owner, Record, _]](column: T) extends Field[Owner, Record, T, UUID](column) {
  def apply(row: UDTValue): Option[UUID] = Option(row.getUUID(name))

  override private[udt] def setSerialise(data: UDTValue): UDTValue = data.setUUID(name, value)
}
