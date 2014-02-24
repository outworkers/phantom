package com.newzly.phantom.example

import java.util.UUID
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._
import com.newzly.phantom.thrift.ThriftColumn
import com.twitter.scrooge.CompactThriftSerializer


case class SampleRecord(
  stuff: String,
  someList: List[String],
  thriftModel: SampleModel
)

sealed class ThriftTable extends CassandraTable[ThriftTable,  SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object stuff extends StringColumn(this)
  object someList extends ListColumn[ThriftTable, SampleRecord, String](this)
  object thriftModel extends ThriftColumn[ThriftTable, SampleRecord, SampleModel](this) {
    def serializer = new CompactThriftSerializer[SampleModel] {
      override def codec = SampleModel
    }
  }


  def fromRow(r: Row): SampleRecord = {
    SampleRecord(stuff(r), someList(r), thriftModel(r))
  }
}
