package com.websudos.phantom.udt

import java.net.InetAddress
import java.util.UUID

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.DynamicVariable

import com.datastax.driver.core.{ Cluster, Row, UserType }
import com.websudos.phantom.CassandraPrimitive
import com.websudos.phantom.column.AbstractColumn

/**
 * A global lock for reflecting and collecting fields inside a User Defined Type.
 * This prevents a race condition and bug.
 */
private[phantom] object Lock

/**
 * A field part of a user defined type.
 * @param owner The UDT column that owns the field.
 * @tparam T The Scala type corresponding the underlying Cassandra type of the UDT field.

sealed abstract class AbstractField[@specialized(Int, Double, Float, Long, Boolean, Short) T](owner: UDT[_]) {
  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")


  protected[this] lazy val valueBox = new DynamicVariable[Option[T]](None)

  protected[this] def typeDef()(implicit cluster: Cluster, keySpace: String): UserType = cluster.getMetadata.getKeyspace(keySpace).getUserType(name)

  def value: T = valueBox.value.getOrElse(null.asInstanceOf[T])

  def cassandraType: String
}


abstract class Field[Owner <: UDT[T], T : CassandraPrimitive](column: Owner) extends AbstractField[T](column) {
  def apply(item: T): Owner = {
    valueBox.value_=(Some(item))
    column
  }

  val cassandraType = implicitly[CassandraPrimitive[T]].cassandraType
}


abstract class UDT[T <: UDT[T]] extends AbstractColumn[T] {

  private[this] lazy val _fields: MutableArrayBuffer[AbstractField[_]] = new MutableArrayBuffer[AbstractField[_]] with MutableSyncBuffer[AbstractField[_]]

  def apply(row: Row): T

  private[this] lazy val _name: String = {
    getClass.getName.split("\\.").toList.last.replaceAll("[^$]*\\$\\$[^$]*\\$[^$]*\\$|\\$\\$[^\\$]*\\$", "").dropRight(1)
  }

  def toCType(v: T): AnyRef = ???

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
class StringField[T <: UDT[T]](column: T) extends Field[T, String](column) {}
class InetField[T <: UDT[T]](column: T)  extends Field[T, InetAddress](column)
class IntField[T <: UDT[T]](column: T)  extends Field[T, Int](column)
class DoubleField[T <: UDT[T]](column: T)  extends Field[T, Double](column)
class LongField[T <: UDT[T]](column: T)  extends Field[T, Long](column)
class BigIntField[T <: UDT[T]](column: T)  extends Field[T, BigInt](column)
class BigDecimalField[T <: UDT[T]](column: T)  extends Field[T, BigDecimal](column)
class UDTField[Owner <: UDT[Owner], T <: UDT[_]](column: Owner) extends Field[Owner, T](column)
class UUIDField[Owner <: UDT[Owner]](column: Owner) extends Field[Owner, UUID](column)
 */
