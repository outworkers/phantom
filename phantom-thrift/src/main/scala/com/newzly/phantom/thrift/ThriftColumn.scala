package com.newzly.phantom.thrift

import scala.collection.breakOut
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.newzly.phantom.column.{OptionalColumn, AbstractColumn, Column}
import com.twitter.scrooge.{ CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try


trait ThriftColumnDefinition[ValueType <: ThriftStruct] {

  /**
   * The Thrift serializer to use.
   * This must always be overriden before usage.
   * It is a simple forwarding from a concrete Thrift Struct.
   */
  def serializer: CompactThriftSerializer[ValueType]

  /**
   * This converts a value to the appropiate Cassandra type.
   * All Thrift structs are serialized to strings.
   * @param v The Thrift struct to convert.
   * @return A string containing the compact Thrift serialization.
   */
  def itemToCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  val primitive = implicitly[CassandraPrimitive[String]]
}


abstract class ThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends Column[T, R, ValueType](table)
  with ThriftColumnDefinition[ValueType] {

  def toCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  val cassandraType = "text"

  def optional(r: Row): Option[ValueType] = {
    Try {
      serializer.fromString(r.getString(name))
    }.toOption
  }
}

abstract class OptionalThriftColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R])
  extends OptionalColumn[T, R, ValueType](table)
  with ThriftColumnDefinition[ValueType] {

  val cassandraType = "text"

  def toCType(v: Option[ValueType]): AnyRef = {
    v map serializer.toString getOrElse null
  }

  def optional(r: Row): Option[ValueType] = {
    Try {
      serializer.fromString(r.getString(name))
    }.toOption
  }

}

abstract class ThriftSetColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R]) extends Column[T, R, Set[ValueType]](table) with ThriftColumnDefinition[ValueType] {
  
  override val cassandraType = "set<text>"

  override def toCType(v: Set[ValueType]): AnyRef = {
    v.map(itemToCType)(breakOut).toSeq.asJava
  }

  override def optional(r: Row): Option[Set[ValueType]] = {
    Option(r.getSet(name, primitive.cls)).map(_.asScala.map(
      e => serializer.fromString(primitive.fromCType(e.asInstanceOf[String]))
    ).toSet[ValueType])
  }
}


abstract class ThriftListColumn[T <: CassandraTable[T, R], R, ValueType <: ThriftStruct](table: CassandraTable[T, R]) extends Column[T, R, List[ValueType]](table) with ThriftColumnDefinition[ValueType] {
  override val cassandraType = "list<text>"

  override def toCType(v: List[ValueType]): AnyRef = {
    v.map(serializer.toString)(breakOut).toList.asJava
  }

  override def optional(r: Row): Option[List[ValueType]] = {
    Option(r.getList(name, primitive.cls)).map(_.asScala.map(
      e => serializer.fromString(primitive.fromCType(e.asInstanceOf[String]))
    ).toList)
  }
}

abstract class ThriftMapColumn[T <: CassandraTable[T, R], R, KeyType : CassandraPrimitive, ValueType <: ThriftStruct](table: CassandraTable[T, R]) extends Column[T, R, Map[KeyType, ValueType]](table) with ThriftColumnDefinition[ValueType] {
  override val cassandraType = s"map<${CassandraPrimitive[KeyType].cassandraType}, text>"

  override def toCType(v: Map[KeyType, ValueType]): AnyRef = {
    mapAsJavaMapConverter(v.map {
      case (key, value) => CassandraPrimitive[KeyType].toCType(key) -> primitive.toCType(serializer.toString(value))
    }).asJava
  }

  def optional(r: Row): Option[Map[KeyType, ValueType]] = {
    val ki = implicitly[CassandraPrimitive[KeyType]]
    Option(r.getMap(name, ki.cls, primitive.cls)).map(_.asScala.map {
      case (k, v) =>
        ki.fromCType(k.asInstanceOf[AnyRef]) -> serializer.fromString(primitive.fromCType(v.asInstanceOf[AnyRef]))
    }(breakOut) toMap)
  }
}