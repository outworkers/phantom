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
package com.websudos.phantom.udt

import com.twitter.util.Future
import com.websudos.phantom.dsl._
import com.websudos.phantom.connectors.SimpleCassandraConnector

case class TestRecord(id: UUID, name: String, address: TestFields.address.type)

trait Connector extends SimpleCassandraConnector {
  val keySpace = "phantom_udt"
}

object Connector extends Connector

sealed class TestFields extends CassandraTable[TestFields, TestRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  object address extends UDTColumn(this) {
    val connector = Connector
    object postCode extends StringField[TestFields, TestRecord, address.type](this)
    object street extends StringField[TestFields, TestRecord, address.type](this)
    object test extends IntField[TestFields, TestRecord, address.type](this)
  }

  def fromRow(row: Row): TestRecord = {
    TestRecord(
      id(row),
      name(row),
      address(row)
    )
  }
}

object TestFields extends TestFields with Connector {

  def getAddress(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.id eqs id).get()
  }

}
