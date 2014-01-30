package com.newzly.phantom.column

import java.nio.charset.Charset
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TMemoryInputTransport
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.twitter.scrooge.{ ThriftStruct, ThriftStructCodec3 }
import com.twitter.util.Try

abstract class ThriftColumn[Owner <: CassandraTable[Owner, Record], Record, ValueType <: ThriftStruct](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, ValueType](table) {
  def toCType(v: ValueType): AnyRef = {
    val _trans = new TMemoryInputTransport()
    v.write(new TBinaryProtocol(_trans))
    v.toString
  }

  def decode(data: Array[Byte]): ValueType

  val cassandraType = "text"

  def optional(r: Row): Option[ValueType] = {
    Try {
      val data = r.getString(name)
      Some(decode(data.getBytes(Charset.forName("UTF-8"))))
    } getOrElse None
  }
}
