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

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit.suites.TestDefaults

import scala.concurrent.Future

abstract class Address[
  T <: CassandraTable[T, R],
  R
](table: CassandraTable[T, R])(implicit connector: KeySpaceDef) extends UDTColumn[T, R, Address[T, R]](table) {

  object postCode extends StringField[T, R, Address[T, R]](this)
  object street extends StringField[T, R, Address[T, R]](this)
  object test extends IntField[T, R, Address[T, R]](this)
}

case class TestRecord(
  id: UUID,
  name: String,
  address: Address[TestFields, TestRecord]
)

abstract class TestFields extends CassandraTable[TestFields, TestRecord] with RootConnector {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  object address extends Address(this)(TestDefaults.defaultConnector)

  def fromRow(row: Row): TestRecord = {
    TestRecord(
      id(row),
      name(row),
      address(row)
    )
  }
}

object TestFields extends TestFields with TestDefaults.defaultConnector.Connector {

  def getAddress(id: UUID): Future[Option[TestRecord]] = {
    select.where(_.id eqs id).one()
  }

}
