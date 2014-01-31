package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.twitter.scrooge.{ CompactThriftSerializer, ThriftStruct}
import com.twitter.util.Try

abstract class ThriftColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, ValueType](table) {

  def toCType(v: ValueType): AnyRef = {
    serializer.toString(v)
  }

  def serializer: CompactThriftSerializer[ValueType]

  val cassandraType = "text"

  def optional(r: Row): Option[ValueType] = {
    Try {
      Some(serializer.fromString(r.getString(name)))
    } getOrElse None
  }
}
