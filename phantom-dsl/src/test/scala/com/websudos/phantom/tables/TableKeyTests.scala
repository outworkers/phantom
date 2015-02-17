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
package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.dsl._
import com.websudos.phantom.PhantomCassandraConnector


case class StubRecord(name: String, id: UUID)
sealed class TableWithSingleKey extends CassandraTable[TableWithSingleKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithSingleKey extends TableWithSingleKey with PhantomCassandraConnector

class TableWithCompoundKey extends CassandraTable[TableWithCompoundKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompoundKey extends TableWithCompoundKey with PhantomCassandraConnector


sealed class TableWithCompositeKey extends CassandraTable[TableWithCompositeKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second_part extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompositeKey extends TableWithCompositeKey with PhantomCassandraConnector

sealed class TableWithNoKey extends CassandraTable[TableWithNoKey, StubRecord] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithNoKey extends TableWithNoKey with PhantomCassandraConnector
