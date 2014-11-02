package com.websudos.phantom.udt

import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.{ResultSet, Row, Session, UDTValue, UserType}
import com.twitter.util.{Future, Try}
import com.websudos.phantom.Implicits.Column
import com.websudos.phantom.query.ExecutableStatement
import com.websudos.phantom.zookeeper.CassandraConnector
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}

import scala.collection.mutable.{ArrayBuffer => MutableArrayBuffer, SynchronizedBuffer => MutableSyncBuffer}
import scala.concurrent.{ExecutionContext, Future => ScalaFuture}
import scala.reflect.runtime.universe.Symbol
import scala.reflect.runtime.{currentMirror => cm, universe => ru}
import scala.util.DynamicVariable


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


/**
 * This is a centralised singleton that collects references to all UDT column definitions in the entire module.
 * It is used to auto-generate the schema of all the UDT columns in a manner that is completely invisible to the user.
 *
 * The synchronisation of the schema is not done automatically, allowing for fine grained control of events,
 * but the auto-generaiton and execution capabilities are available with a single method call.
 */
private[udt] object UDTCollector {
  private[this] val _udts = MutableArrayBuffer.empty[UDTDefinition[_]]

  def push[T](udt: UDTDefinition[T]): Unit = {
    _udts += udt
  }

  /**
   * This is a working version of an attempt to combine all UDT creation futures in a single result.
   * This way, the end user can await for a single result with a single Future before being able to use the entire set of UDT definitions.
   *
   * @param session The Cassandra database connection session.
   * @return
   */
  def future()(implicit session: Session, ec: ExecutionContext): ScalaFuture[ResultSet] = {
    ScalaFuture.sequence(_udts.toSeq.map(_.create().future())).map(_.head)
  }

  def execute()(implicit session: Session): Future[ResultSet] = {
    Future.collect(_udts.map(_.create().execute())).map(_.head)
  }
}


sealed trait UDTDefinition[T] {
  def name: String

  def fields: List[AbstractField[_]] = _fields.toList

  def connector: CassandraConnector

  def typeDef: UserType = connector.manager.cluster.getMetadata.getKeyspace(connector.keySpace).getUserType(name)

  val cassandraType = name.toLowerCase

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

    UDTCollector.push(this)
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

  def create(): UDTCreateQuery = new UDTCreateQuery(null, this)

  /**
   * Much like the definition of a Cassandra table where the columns are collected, the fields of an UDT are collected inside this buffer.
   * Every new buffer spawned will be a perfect clone of this instance, and the fields will always be pre-initialised on extraction.
   */
  private[udt] lazy val _fields: MutableArrayBuffer[AbstractField[_]] = new MutableArrayBuffer[AbstractField[_]] with MutableSyncBuffer[AbstractField[_]]
}


abstract class UDTColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  T <: UDTColumn[Owner, Record, T]
](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, T](table) with UDTDefinition[T] {

   override def apply(row: Row): T = {
    val instance: T = clone().asInstanceOf[T]
    val data = row.getUDTValue(name)

    instance.fields.foreach(field => {
      field.set(data)
    })
    instance
  }

  override def optional(r: Row): Option[T] = {
    Try {
      val instance: T = clone().asInstanceOf[T]
      val data = r.getUDTValue(name)

      instance.fields.foreach(field => {
        field.set(data)
      })

      instance
    }.toOption
  }

  def toCType(v: T): AnyRef = {
    val data = typeDef.newValue()
    fields.foreach(field => {
      field.setSerialise(data)
    })
    data.toString
  }
}

sealed class UDTCreateQuery(val qb: BuiltStatement, udt: UDTDefinition[_]) extends ExecutableStatement {

  override def execute()(implicit session: Session): Future[ResultSet] = {
    twitterQueryStringExecuteToFuture(udt.schema())
  }

  override def future()(implicit session: Session): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(udt.schema())
  }
}


