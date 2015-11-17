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

import akka.actor.ActorSystem
import com.websudos.phantom.batch.BatchType
import com.websudos.phantom.dsl._
import org.reactivestreams.Publisher
import play.api.libs.iteratee.Enumerator
import play.api.libs.streams.Streams
import scala.concurrent.duration.FiniteDuration

/**
 * Just a wrapper module for enhancing phantom [[CassandraTable]]
 * with reactive streams features.
 *
 * In order to be used, please be sured to import the implicits
 * into the scope.
 *
 * {{{
 * import ReactiveCassandra._
 * val subscriber = CassandraTableInstance.subscriber()
 * }}}
 *
 * @see [[http://www.reactive-streams.org/]]
 * @see [[https://github.com/websudos/phantom]]
 */
package object reactivestreams {

  private[this] final val DEFAULT_CONCURRENT_REQUESTS = 5

  private[this] final val DEFAULT_BATCH_SIZE = 100
  /**
   * @tparam CT the concrete type inheriting from [[CassandraTable]]
   * @tparam T the type of the streamed element
   */
  implicit class StreamedCassandraTable[CT <: CassandraTable[CT, T], T](val ct: CassandraTable[CT, T]) extends AnyVal {


    /**
     * Gets a reactive streams [[org.reactivestreams.Subscriber]] with
     * batching capabilities for some phantom [[CassandraTable]]. This
     * subscriber is able to work for both finite short-lived streams
     * and never-ending long-lived streams. For the latter, a flushInterval
     * parameter can be used.
     *
     * @param batchSize the number of elements to include in the Cassandra batch
     * @param concurrentRequests the number of concurrent batch operations
     * @param batchType the type of the batch.
     *                  @see See [[http://docs.datastax.com/en/cql/3.1/cql/cql_reference/batch_r.html]] for further
     *                       explanation.
     * @param flushInterval used to schedule periodic batch execution even though the number of statements hasn't
     *                      been reached yet. Useful in never-ending streams that will never been completed.
     * @param completionFn a function that will be invoked when the stream is completed
     * @param errorFn a function that will be invoked when an error occurs
     * @param builder an implicitly resolved [[RequestBuilder]] that wraps a phantom [[com.websudos.phantom.builder.query.ExecutableStatement]].
     *                Every T element that gets into the stream from the upstream is turned into a ExecutableStatement
     *                by means of this builder.
     * @param system the underlying [[ActorSystem]]. This [[org.reactivestreams.Subscriber]] implementation uses Akka
     *               actors, but is not restricted to be used in the context of Akka Streams.
     * @param session the Cassandra [[com.datastax.driver.core.Session]]
     * @param space the Cassandra [[KeySpace]]
     * @param ev an evidence to get the T type removed by erasure
     * @return the [[org.reactivestreams.Subscriber]] to be connected to a reactive stream typically initiated by
     *         a [[org.reactivestreams.Publisher]]
     */
    def subscriber(
      batchSize: Int = DEFAULT_BATCH_SIZE,
      concurrentRequests: Int = DEFAULT_CONCURRENT_REQUESTS,
      batchType: BatchType = BatchType.Unlogged,
      flushInterval: Option[FiniteDuration] = None,
      completionFn: () => Unit = () => (),
      errorFn: Throwable => Unit = _ => ()
    )(implicit
      builder: RequestBuilder[CT, T],
      system: ActorSystem,
      session: Session,
      space: KeySpace,
      ev: Manifest[T]
    ): BatchSubscriber[CT, T] = {
      new BatchSubscriber[CT, T](
        ct.asInstanceOf[CT],
        builder,
        batchSize,
        concurrentRequests,
        batchType,
        flushInterval,
        completionFn,
        errorFn
      )
    }

    /**
      * Creates a stream publisher based on the default ReactiveStreams implementation.
      * This will use the underlying Play enumerator model to convert.
      *
      * @param session The Cassandra session to execute the enumeration within.
      * @param keySpace The target keyspace.
      * @return A publisher of records, publishing one record at a time.
      */
    def publisher()(implicit session: Session, keySpace: KeySpace): Publisher[T] = {
      Streams.enumeratorToPublisher(ct.select.all().fetchEnumerator())
    }
  }

  implicit class PublisherConverter[T](val enumerator: Enumerator[T]) extends AnyVal {

    def publisher: Publisher[T] = {
      Streams.enumeratorToPublisher(enumerator)
    }
  }

  /**
    * Returns the product of the arguments,
    * throwing an exception if the result overflows a {@code long}.
    *
    * @param x the first value
    * @param y the second value
    * @return the result
    * @throws ArithmeticException if the result overflows a long
    * @since 1.8
    */
  def multiplyExact(x: Long, y: Long): Long = {
    val r: Long = x * y
    val ax: Long = Math.abs(x)
    val ay: Long = Math.abs(y)
    if (((ax | ay) >> 31) != 0) {
      if (((y != 0) && (r / y != x)) || (x == Long.MinValue && y == -1)) {
        throw new ArithmeticException("long overflow")
      }
    }
    r
  }

}
