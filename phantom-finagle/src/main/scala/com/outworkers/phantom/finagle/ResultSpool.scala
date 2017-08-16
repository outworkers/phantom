/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.finagle

import com.datastax.driver.core.{ResultSet => DatastaxResultSet}
import com.twitter.concurrent.Spool
import com.twitter.util.{Future, Promise, Try}
import com.google.common.util.concurrent.{
  ListenableFuture,
  MoreExecutors,
  Uninterruptibles
}

/**
  * Wrapper for creating Spools of Rows
  */
private[phantom] object ResultSpool {
  def loop(it: Iterator[Row], rs: ResultSet): Spool[Seq[Row]] = {
    if (rs.isExhausted) {
      Spool.empty[Seq[Row]]
    } else {
      val buf = new Array[Row](rs.getAvailableWithoutFetching)
      it.copyToArray(buf)
      val head = buf.toSeq
      val more = rs.fetchMoreResults

      head *:: {
        val p = Promise[DatastaxResultSet]
        more.addListener(new TFutureListener(more, p), MoreExecutors.directExecutor)
        p.map(_ => loop(it, rs))
      }
    }
  }

  /**
    * Create a Spool of Rows.
    *
    * Things to make sure:
    *   1) We don't block!
    *   2) If we don't have anything else to do we submit to the thread pool and
    *      wait to get called back and chain onto that.
    */
  def spool(rs: ResultSet): Future[Spool[Seq[Row]]] = {
    val it = rs.iterate()
    Future.value(loop(it, rs))
  }
}

private[phantom] class TFutureListener[A](
  future: ListenableFuture[A],
  promise: Promise[A]
) extends Runnable {
  def run: Unit = {
    promise.update {
      Try {
        if (!future.isDone)
          throw new IllegalStateException("future not complete")
        Uninterruptibles.getUninterruptibly(future)
      }
    }
  }
}
