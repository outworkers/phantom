/*
 * Copyright 2013 - 2017 Outworkers Ltd.
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
package com.outworkers.phantom.example.basics

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.thrift._

// Sample model here comes from the Thrift struct definition.
// The IDL is available in phantom-example/src/main/thrift.
case class SampleRecord(
  stuff: String,
  someList: List[String],
  thriftModel: SampleModel
)

abstract class ThriftTable extends CassandraTable[ThriftTable, SampleRecord] with RootConnector {
  object id extends UUIDColumn(this) with PartitionKey
  object stuff extends StringColumn(this)
  object someList extends ListColumn[String](this)

  // By default, com.outworkers.phantom will use a compact Thrift serializer.
  // And store the records as strings in Cassandra.
  object thriftModel extends ThriftColumn[ThriftTable, SampleRecord, SampleModel](this)
}
