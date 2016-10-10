/*
 * Copyright 2013-2016 Outworkers, Limited.
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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.tables

import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class PrimaryCollectionRecord(
  index: List[String],
  set: Set[String],
  map: Map[String, String],
  name: String,
  value: Int
)

class PrimaryCollectionTable extends CassandraTable[ConcretePrimaryCollectionTable, PrimaryCollectionRecord] {
  object listIndex extends ListColumn[String](this) with PartitionKey[List[String]]
  object setCol extends SetColumn[String](this) with PrimaryKey[Set[String]]
  object mapCol extends MapColumn[String, String](this) with PrimaryKey[Map[String, String]]
  object name extends StringColumn(this) with PrimaryKey[String]
  object value extends IntColumn(this)

  def fromRow(row: Row): PrimaryCollectionRecord = {
    PrimaryCollectionRecord(
      listIndex(row),
      setCol(row),
      mapCol(row),
      name(row),
      value(row)
    )
  }
}

abstract class ConcretePrimaryCollectionTable extends PrimaryCollectionTable with RootConnector {

  def store(rec: PrimaryCollectionRecord): Future[ResultSet] = {
    insert.value(_.listIndex, rec.index)
      .value(_.setCol, rec.set)
      .value(_.mapCol, rec.map)
      .value(_.name, rec.name)
      .value(_.value, rec.value)
      .future()
  }
}