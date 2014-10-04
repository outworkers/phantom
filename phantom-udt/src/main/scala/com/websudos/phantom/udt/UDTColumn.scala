package com.websudos.phantom.udt

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.DynamicVariable

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

  lazy val name: String = cm.reflect(this).symbol.name.toTypeName.decoded

  protected[udt] lazy val valueBox = new DynamicVariable[Option[T]](None)

  def value: T = valueBox.value.getOrElse(null.asInstanceOf[T])

  private[udt] def setSerialise(data: UDTValue): UDTValue

  private[udt] def set(value: Option[T]): Unit = valueBox.value_=(value)

  private[udt] def set(data: UDTValue): Unit = valueBox.value_=(apply(data))

  def cassandraType: String = CassandraPrimitive[T].cassandraType

  def apply(row: UDTValue): Option[T]
}


private[udt] abstract class Field[
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

  type UDT = T

  /**
   * Much like the definition of a Cassandra table where the columns are collected, the fields of an UDT are collected inside this buffer.
   * Every new buffer spawned will be a perfect clone of this instance, and the fields will always be pre-initialised on extraction.
   */
  private[udt] lazy val _fields: MutableArrayBuffer[AbstractField[_]] = new MutableArrayBuffer[AbstractField[_]] with MutableSyncBuffer[AbstractField[_]]

  def fields: List[AbstractField[_]] = _fields.toList

  def connector: CassandraConnector

  private[this] def typeDef: UserType = connector.manager.cluster.getMetadata.getKeyspace(connector.keySpace).getUserType(name)

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

  private[this] lazy val _name: String = cm.reflect(this).symbol.name.toTypeName.decoded


  def toCType(v: T): AnyRef = {
    val data = typeDef.newValue()
    fields.foreach(field => {
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
    val queryInit = s"CREATE TYPE IF NOT EXISTS $name("
    val queryColumns = _fields.foldLeft("")((qb, c) => {
      if (qb.isEmpty) {
        s"${c.name} ${c.cassandraType}"
      } else {
        s"$qb, ${c.name} ${c.cassandraType}"
      }
    })
    queryInit + queryColumns + ");"
  }
}


