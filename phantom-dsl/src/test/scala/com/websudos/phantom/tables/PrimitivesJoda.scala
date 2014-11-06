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
package com.websudos.phantom.tables

import org.joda.time.DateTime

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraConnector

case class JodaRow(
  pkey: String,
  int: Int,
  bi: DateTime
)

sealed class PrimitivesJoda extends CassandraTable[PrimitivesJoda, JodaRow] {
  override def fromRow(r: Row): JodaRow = {
    JodaRow(pkey(r), intColumn(r), timestamp(r))
  }

  object pkey extends StringColumn(this) with PartitionKey[String]
  object intColumn extends IntColumn(this)
  object timestamp extends DateTimeColumn(this)
}

object PrimitivesJoda extends PrimitivesJoda with PhantomCassandraConnector {

  override val tableName = "PrimitivesJoda"

}

