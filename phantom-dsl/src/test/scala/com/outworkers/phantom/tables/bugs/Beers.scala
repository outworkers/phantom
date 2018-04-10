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
package com.outworkers.phantom.tables.bugs

import com.outworkers.phantom.dsl._

import scala.concurrent.Future

case class Beer(company: String, name: String, style: String)

abstract class Beers extends Table[Beers, Beer] {
  object company extends StringColumn with PartitionKey
  object name extends StringColumn with PartitionKey
  object style extends StringColumn

  def save(beer: Beer): Future[ResultSet] = {
    insert
      .value(_.company, beer.company)
      .value(_.name, beer.name)
      .value(_.style, beer.style)
      .future()
  }

  def all(): Future[List[Beer]] = {
    select.all.fetch()
  }
}

class CassandraDatabase(override val connector: CassandraConnection) extends Database[CassandraDatabase](connector) {
  object beers extends Beers with Connector

  beers.storeRecord(Beer("a", "b", "c"))
}
object CassandraDatabase extends CassandraDatabase(ContactPoint.local.keySpace(KeySpace("outworkers")
  .ifNotExists().`with`(replication eqs SimpleStrategy.replication_factor(1))))

