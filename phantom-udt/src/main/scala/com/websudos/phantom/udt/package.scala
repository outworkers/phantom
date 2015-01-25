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
package com.websudos.phantom

import scala.concurrent.{Future => ScalaFuture, ExecutionContext}
import com.datastax.driver.core.{Session, ResultSet}
import com.twitter.util.Future

package object udt {

  type BooleanField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.BooleanField[Owner,
    Record, Col]

  type BigIntField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.BigIntField[Owner,
    Record, Col]

  type BigDecimalField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record,
    Col]] = com.websudos.phantom.udt.Fields.BigDecimalField[Owner, Record, _]

  type UUIDField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record,
    Col]] = com.websudos.phantom.udt.Fields.UUIDField[Owner, Record, _]


  type StringField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.StringField[Owner,
    Record, Col]

  type IntField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.IntField[Owner,
    Record, Col]

  type InetField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.InetField[Owner,
    Record, Col]

  type DoubleField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.DoubleField[Owner,
    Record, Col]

  type LongField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.LongField[Owner,
    Record, Col]

  type DateField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.DateField[Owner,
    Record, Col]

  type DateTimeField[Owner <: CassandraTable[Owner, Record], Record, Col <: UDTColumn[Owner, Record, _]] = com.websudos.phantom.udt.Fields.DateTimeField[Owner,
    Record, Col]

  implicit class CassandraUDT[T <: CassandraTable[T, R], R](val table: CassandraTable[T, R]) extends AnyVal {
    def udtExecute()(implicit session: Session): Future[ResultSet] = {
      UDTCollector.execute()
    }

    def udtFuture()(implicit session: Session, ec: ExecutionContext): ScalaFuture[ResultSet] = {
      UDTCollector.future()
    }
  }



}
