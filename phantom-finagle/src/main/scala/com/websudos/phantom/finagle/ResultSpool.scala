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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.finagle

import com.datastax.driver.core.{ResultSet, Row}
import com.twitter.concurrent.Spool
import com.twitter.util.{FuturePool, Future => TFuture}

import scala.collection.JavaConversions._

/**
  * Wrapper for creating Spools of Rows
  */
private[phantom] object ResultSpool {
  lazy val pool = FuturePool.unboundedPool

  def loop(head: Row, it: Iterator[Row], rs: ResultSet): Spool[Row] = {
    lazy val tail =
      if (rs.isExhausted) {
        TFuture.value(Spool.empty)
      } else {
        val a = rs.getAvailableWithoutFetching

        // 100 is somewhat arbitrary. In practice it might not matter that much
        // but it should be tested.
        if (a < 100 && !rs.isFullyFetched) {
          rs.fetchMoreResults
        }

        if (a > 0) {
          TFuture.value(loop(it.next(), it, rs))
        } else {
          pool(it.next()).map(x => loop(x, it, rs))
        }
      }

    head *:: tail
  }

  /**
    * Create a Spool of Rows.
    *
    * Things to make sure:
    *   1) We don't block!
    *   2) If we don't have anything else to do we submit to the thread pool and
    *      wait to get called back and chain onto that.
    */
  def spool(rs: ResultSet): TFuture[Spool[Row]] = {
    val it = rs.iterator
    if (!rs.isExhausted) {
      pool(it.next).map(x => loop(x, it, rs))
    } else {
      TFuture.value(Spool.empty)
    }
  }
}

