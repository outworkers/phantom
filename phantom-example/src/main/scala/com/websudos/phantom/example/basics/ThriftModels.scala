/*
 * Copyright 2013 websudos ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.websudos.phantom.example.basics

import java.util.UUID
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.thrift.ThriftColumn
import com.twitter.scrooge.CompactThriftSerializer

// Sample model here comes from the Thrift struct definition.
// The IDL is available in com.websudos.phantom-example/src/main/thrift.
case class SampleRecord(
  stuff: String,
  someList: List[String],
  thriftModel: SampleModel
)

sealed class ThriftTable extends CassandraTable[ThriftTable,  SampleRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object stuff extends StringColumn(this)
  object someList extends ListColumn[ThriftTable, SampleRecord, String](this)


  // As you can see, com.websudos.phantom will use a compact Thrift serializer.
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

object ThriftTable extends ThriftTable with ExampleConnector {

}
