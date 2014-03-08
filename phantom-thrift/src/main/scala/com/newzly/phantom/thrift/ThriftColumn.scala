package com.newzly.phantom.thrift

import scala.collection.breakOut
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.newzly.phantom.column.{ Column, SetColumn }
import com.twitter.scrooge.{ CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try


trait ThriftColumnDefinition[ValueType <: ThriftStruct] {
  /**
   * The Thrift serializer to use.
   * This must always be overriden before usage.
   * It is a simple forwarding from a concrete Thrift Struct.
   */
  def serializer: CompactThriftSerializer[ValueType]

  def itemToCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  val primitive = implicitly[CassandraPrimitive[String]]
}


abstract class ThriftColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, ValueType](table) with ThriftColumnDefinition[ValueType] {

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

abstract class ThriftSetColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Set[ValueType]](table) with ThriftColumnDefinition[ValueType] {
  def serializer: CompactThriftSerializer[ValueType]

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

abstract class ThriftListColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, List[ValueType]](table) with ThriftColumnDefinition[ValueType] {
  def serializer: CompactThriftSerializer[ValueType]

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