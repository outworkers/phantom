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
package com.outworkers.phantom.tables

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._

 case class SecondaryIndexRecord(
  primary: UUID,
  secondary: UUID,
  name: String
)

sealed class SecondaryIndexTable extends CassandraTable[ConcreteSecondaryIndexTable, SecondaryIndexRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object secondary extends UUIDColumn(this) with Index[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): SecondaryIndexRecord = {
    SecondaryIndexRecord(
      primary = id(r),
      secondary = secondary(r),
      name = name(r)
    )
  }
}

abstract class ConcreteSecondaryIndexTable extends SecondaryIndexTable with RootConnector {

  def store(sample: SecondaryIndexRecord): InsertQuery.Default[ConcreteSecondaryIndexTable, SecondaryIndexRecord] = {
    insert
      .value(_.id, sample.primary)
      .value(_.secondary, sample.secondary)
      .value(_.name, sample.name)
  }

}