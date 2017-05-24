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
package com.outworkers.phantom.macros

import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import Strategy._

abstract class NamedArticlesByAuthor extends Table[NamedArticlesByAuthor, Article] {
  object id extends UUIDColumn with PartitionKey
  object name extends StringColumn
  object orderId extends LongColumn
}

class NamingStrategyDatabase(
  override val connector: CassandraConnection
) extends Database[NamingStrategyDatabase](connector) {

  object articlesByAuthor extends NamedArticlesByAuthor with Connector
}

object NamingStrategyDatabase extends NamingStrategyDatabase(Connector.default)