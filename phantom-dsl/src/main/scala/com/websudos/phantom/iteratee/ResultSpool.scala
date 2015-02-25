/*
 * Copyright 2015 websudos ltd.
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
 *
 * Contributor(s):
 *   Ben Edwards
 */
package com.websudos.phantom.iteratee

import scala.collection.JavaConversions._

import com.datastax.driver.core.{ ResultSet, Row }
import com.twitter.concurrent.Spool
import com.twitter.util.{ FuturePool, Future => TFuture }

/**
 * Wrapper for creating Spools of Rows
 */
private[phantom] object ResultSpool {
  lazy val pool = FuturePool.unboundedPool

  def loop(
    head: Row,
    it: Iterator[Row],
    rs: ResultSet): Spool[Row] =
  {
    lazy val tail =
      if (rs.isExhausted)
        TFuture.value(Spool.empty)
      else {
        val a = rs.getAvailableWithoutFetching

        // 100 is somewhat arbitrary. In practice it might not matter that much
        // but it should be tested.
        if (a < 100 && !rs.isFullyFetched)
          rs.fetchMoreResults

        if (a > 0)
          TFuture.value(loop(it.next(), it, rs))
        else
          pool(it.next()).map(x => loop(x, it, rs))
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
    if (!rs.isExhausted)
      pool(it.next).map(x => loop(x, it, rs))
    else
      TFuture.value(Spool.empty)
  }
}

