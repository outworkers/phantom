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
package com.outworkers.phantom.tables

import com.datastax.driver.core.Row
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

class MultipleKeys extends CassandraTable[ConcreteMultipleKeys, Option[MultipleKeys]] {

  object pkey extends StringColumn(this) with PartitionKey
  object intColumn1 extends IntColumn(this) with PrimaryKey with Index
  object intColumn2 extends IntColumn(this) with PrimaryKey
  object intColumn3 extends IntColumn(this) with PrimaryKey
  object intColumn4 extends IntColumn(this) with PrimaryKey
  object intColumn5 extends IntColumn(this) with PrimaryKey
  object intColumn6 extends IntColumn(this) with PrimaryKey
  object intColumn7 extends IntColumn(this) with PrimaryKey
  object timestamp8 extends DateTimeColumn(this)

  override def fromRow(r: Row): Option[MultipleKeys] = None
}

abstract class ConcreteMultipleKeys extends MultipleKeys with RootConnector {
  override def tableName(implicit strategy: NamingStrategy): String = "AJ"
}
