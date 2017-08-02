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
package com.outworkers.phantom.database

import com.datastax.driver.core.Session
import com.outworkers.phantom.{CassandraTable, Manager}
import com.outworkers.phantom.connectors.{CassandraConnection, KeySpace}
import com.outworkers.phantom.macros.DatabaseHelper

import scala.concurrent.blocking

abstract class Database[
  DB <: Database[DB]
](val connector: CassandraConnection)(implicit helper: DatabaseHelper[DB]) {

  trait Connector extends connector.Connector

  implicit val space: KeySpace = KeySpace(connector.name)

  implicit lazy val session: Session = connector.session

  val tables: Seq[CassandraTable[_, _]] = helper.tables(this.asInstanceOf[DB])

  def shutdown(): Unit = {
    blocking {
      Manager.shutdown()
      session.getCluster.close()
      session.close()
    }
  }
}

