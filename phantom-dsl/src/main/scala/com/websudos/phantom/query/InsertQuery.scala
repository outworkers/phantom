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
package com.websudos.phantom.query

import com.twitter.util.Duration

import scala.concurrent.duration.{ Duration => ScalaDuration }
import scala.util.Try
import org.joda.time.DateTime

import com.datastax.driver.core.querybuilder.{ Insert, QueryBuilder }
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.column.AbstractColumn

class InsertQuery[T <: CassandraTable[T, R], R](table: T, val qb: Insert) extends CQLQuery[InsertQuery[T, R]] with BatchableQuery[InsertQuery[T, R]] {

  final def value[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, col.toCType(value)))
  }

  final def valueOrNull[RR](c: T => AbstractColumn[RR], value: RR): InsertQuery[T, R] = {
    val col = c(table)
    new InsertQuery[T, R](table, qb.value(col.name, Try {
      col.toCType(value)
    } getOrElse null.asInstanceOf[T]))
  }

  final def ifNotExists[RR](): InsertQuery[T, R] = {
    new InsertQuery[T, R](table, qb.ifNotExists())
  }

  /**
   * Sets a timestamp expiry for the inserted column.
   * This value is set in seconds.
   * @param expiry The expiry time.
   * @return
   */
  def ttl(expiry: Int): InsertQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry))
    this
  }

  def ttl(expiry: Duration): InsertQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry.inSeconds))
    this
  }

  def ttl(expiry: ScalaDuration): InsertQuery[T, R] = {
    qb.using(QueryBuilder.ttl(expiry.toSeconds.toInt))
    this
  }

  def timestamp(l: Long): InsertQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(l))
    this
  }

  def timestamp(time: DateTime): InsertQuery[T, R] = {
    qb.using(QueryBuilder.timestamp(time.getMillis))
    this
  }
}
