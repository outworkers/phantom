/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
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
