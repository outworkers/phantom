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

import com.datastax.driver.core.Session
import com.websudos.phantom.builder.LimitBound
import com.websudos.phantom.builder.query.{ExecutableQuery, RootSelectBlock}
import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.reactivestreams.iteratee.Enumerator
import play.api.libs.iteratee.{Enumeratee, Enumerator}

import scala.concurrent.ExecutionContext


package object finagle {

  implicit class RootSelectBlockEnumerator[
  T <: CassandraTable[T, _],
  R
  ](val block: RootSelectBlock[T, R]) extends AnyVal {
    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param keySpace The keyspace object in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator()(implicit session: Session, keySpace: KeySpace, ctx: ExecutionContext): PlayEnumerator[R] = {
      val eventualEnum = block.all().future() map {
        resultSet => Enumerator.enumerator(resultSet) through Enumeratee.map(block.fromRow)
      }
      PlayEnumerator.flatten(eventualEnum)
    }
  }

  // trait ExecutableQuery[T <: CassandraTable[T, _], R, Limit <: LimitBound]
  implicit class ExecutableQueryStreamsAugmenter[
  T <: CassandraTable[T, _],
  R,
  Limit <: LimitBound
  ](val query: ExecutableQuery[T, R, Limit]) extends AnyVal {

    /**
      * Produces an Enumerator for [R]ows
      * This enumerator can be consumed afterwards with an Iteratee
      *
      * @param session The Cassandra session in use.
      * @param keySpace The keyspace object in use.
      * @param ctx The Execution Context.
      * @return
      */
    def fetchEnumerator()(implicit session: Session, keySpace: KeySpace, ctx: ExecutionContext): PlayEnumerator[R] = {
      val eventualEnum = query.future() map {
        resultSet => Enumerator.enumerator(resultSet) through Enumeratee.map(query.fromRow)
      }
      PlayEnumerator.flatten(eventualEnum)
    }
  }
}
/*
  /**
   * Produces a [[com.twitter.concurrent.Spool]] of [R]ows
   * A spool is both lazily constructed and consumed, suitable for large
   * collections when using twitter futures.
 *
   * @param session The cassandra session in use.
   * @return A Spool of R.
   */
  def fetchSpool()(implicit session: Session, keySpace: KeySpace): TwitterFuture[Spool[R]] = {
    execute() flatMap {
      resultSet => ResultSpool.spool(resultSet).map(spool => spool map fromRow)
    }
  }

 */