/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.example.basics

import java.util.UUID
import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.twitter.scrooge.CompactThriftSerializer
import com.websudos.phantom.thrift.columns.ThriftColumn

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
