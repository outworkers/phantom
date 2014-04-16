package com.newzly.phantom.example.basics

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.thrift.ThriftColumn
import com.twitter.scrooge.CompactThriftSerializer

// Sample model here comes from the Thrift struct definition.
// The IDL is available in phantom-example/src/main/thrift.
case class SampleRecord(
  stuff: String,
  someList: List[String],
  thriftModel: SampleModel
)

sealed class ThriftTable extends CassandraTable[ThriftTable,  SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object stuff extends StringColumn(this)
  object someList extends ListColumn[ThriftTable, SampleRecord, String](this)


  // As you can see, phantom will use a compact Thrift serializer.
  // And store the records as strings in Cassandra.
  object thriftModel extends ThriftColumn[ThriftTable, SampleRecord, SampleModel](this) {
    def serializer = new CompactThriftSerializer[SampleModel] {
      override def codec = SampleModel
    }
  }

  def fromRow(r: Row): SampleRecord = {
    SampleRecord(stuff(r), someList(r), thriftModel(r))
  }
}
