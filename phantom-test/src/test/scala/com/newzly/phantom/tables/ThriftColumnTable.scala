package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.TestSampler
import com.newzly.phantom.keys.PartitionKey
import com.newzly.phantom.thrift._
import com.twitter.scrooge.CompactThriftSerializer

case class Output(
  id: Int, name: String,
  struct: ThriftTest,
  list: Set[ThriftTest],
  thriftList: List[ThriftTest],
  thriftMap: Map[String, ThriftTest],
  optThrift: Option[ThriftTest],
  count: BigInt
)

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

  object thriftList extends ThriftListColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      def codec = ThriftTest
    }
  }

  object thriftMap extends ThriftMapColumn[ThriftColumnTable, Output, String, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      def codec = ThriftTest
    }
  }

  object optionalThrift extends OptionalThriftColumn[ThriftColumnTable, Output, ThriftTest](this) {
    val serializer = new CompactThriftSerializer[ThriftTest] {
      def codec = ThriftTest
    }
  }

  object count_entries extends CounterColumn(this)

  def fromRow(row: Row): Output = {
    Output(
      id(row),
      name(row),
      ref(row),
      thriftSet(row),
      thriftList(row),
      thriftMap(row),
      optionalThrift(row),
      count_entries(row)
    )
  }
}

object ThriftColumnTable extends ThriftColumnTable with TestSampler[ThriftColumnTable, Output] {}
