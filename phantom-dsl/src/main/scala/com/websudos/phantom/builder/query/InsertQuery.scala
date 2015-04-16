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
package com.websudos.phantom.builder.query

import com.datastax.driver.core.{ResultSet, Session}
import com.twitter.util.{ Future => TwitterFuture }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder._
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace
import org.joda.time.DateTime

import scala.concurrent.{ Future => ScalaFuture }

class InsertQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](table: Table, val init: CQLQuery, clauses: List[(String, String)] = Nil, added: Boolean = false) extends ExecutableStatement with Batchable {

  final def value[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status] = {
    new InsertQuery(table, init, (col(table).name, col(table).asCql(value)) :: clauses, added)
  }

  final def valueOrNull[RR](col: Table => AbstractColumn[RR], value: RR) : InsertQuery[Table, Record, Status] = {
    val insertValue = if (value != null) col(table).asCql(value) else null.asInstanceOf[String]

    new InsertQuery(table, init, (col(table).name, insertValue) :: clauses, added)
  }

  private def terminate: InsertQuery[Table, Record, Status] = {
    if (added) {
      this
    } else {
      new InsertQuery[Table, Record, Status](table, QueryBuilder.insert(init, QueryBuilder.insertPairs(clauses.reverse)), Nil, true).terminate
    }
  }

  override def qb: CQLQuery = {
    if (added) {
      init
    } else {
      terminate.qb
    }
  }

  override def future()(implicit session: Session, keySpace: KeySpace): ScalaFuture[ResultSet] = {
    scalaQueryStringExecuteToFuture(queryString)
  }

  override def execute()(implicit session: Session, keySpace: KeySpace): TwitterFuture[ResultSet] = {
    twitterQueryStringExecuteToFuture(queryString)
  }

  def ttl(seconds: Long): InsertQuery[Table, Record, Status] = {
    new InsertQuery(table, QueryBuilder.ttl(terminate.qb, seconds.toString), clauses, true)
  }

  def ttl(seconds: scala.concurrent.duration.FiniteDuration): InsertQuery[Table, Record, Status] = {
    ttl(seconds.toSeconds)
  }

  def ttl(duration: com.twitter.util.Duration): InsertQuery[Table, Record, Status] = {
    ttl(duration.inSeconds)
  }

  final def timestamp(value: Long): InsertQuery[Table, Record, Status] = {
    new InsertQuery(table, QueryBuilder.using(QueryBuilder.timestamp(terminate.qb, value.toString)), clauses, true)
  }

  final def timestamp(value: DateTime): InsertQuery[Table, Record, Status] = {
    timestamp(value.getMillis)
  }

  def ifNotExists(): InsertQuery[Table, Record, Status] = {
    new InsertQuery(table, QueryBuilder.ifNotExists(terminate.qb), clauses, true)
  }

}

object InsertQuery {

  type Default[T <: CassandraTable[T, _], R] = InsertQuery[T, R, Unspecified]

  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = {
    new InsertQuery[T, R, Unspecified](table, QueryBuilder.insert(QueryBuilder.keyspace(keySpace.name, table.tableName)))
  }
}
