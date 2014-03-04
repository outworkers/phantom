package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.TestSampler
import com.newzly.phantom.keys.PartitionKey
import com.newzly.phantom.thrift.{ ThriftSetColumn, ThriftColumn, ThriftTest }
import com.twitter.scrooge.CompactThriftSerializer

case class Output(id: Int, name: String, struct: ThriftTest, list: Set[ThriftTest])

sealed class ThriftColumnTable extends CassandraTable[ThriftColumnTable, Output] {

  object id extends IntColumn(this) with PartitionKey[Int]
  object name extends StringColumn(this)
  object ref extends ThriftColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      def codec = ThriftTest
    }
  }

  object thriftSet extends ThriftSetColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      def codec = ThriftTest
    }
  }

  def fromRow(row: Row): Output = {
    Output(id(row), name(row), ref(row), thriftSet(row))
  }
}

object ThriftColumnTable extends ThriftColumnTable with TestSampler[ThriftColumnTable, Output] {}
