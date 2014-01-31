package com.newzly.phantom.column

import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.twitter.scrooge.{ CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try

abstract class ThriftColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, ValueType](table) {

  /**
   * The Thrift serializer to use.
   * This must always be overriden before usage.
   * It is a simple forwarding from a concrete Thrift Struct.
   */
  def serializer: CompactThriftSerializer[ValueType]

  def toCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }


  val cassandraType = "text"

  def optional(r: Row): Option[ValueType] = {
    Try {
      Some(serializer.fromString(r.getString(name)))
    } getOrElse None
  }
}


abstract class ThriftSeqColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Seq[ValueType]](table) {

  def serializer: CompactThriftSerializer[ValueType]

  val cassandraType = "list<text>"

  override def toCType(v: Seq[ValueType]): AnyRef = {
    v.map(serializer.toString).toSeq.asJava
  }

  def optional(r: Row): Option[Seq[ValueType]] = {
    val i = implicitly[CassandraPrimitive[String]]

    Option(r.getList(name, i.cls)).map(_.asScala.map(
      e => serializer.fromString(i.fromCType(e.asInstanceOf[String]))
    ).toSeq)
  }
}