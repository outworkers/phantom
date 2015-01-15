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
package com.websudos.phantom.iteratee

import java.util.{ ArrayDeque => JavaArrayDeque, Deque => JavaDeque }

import scala.concurrent.{ ExecutionContext, Future }
import scala.collection.JavaConversions._

import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.Manager
import play.api.libs.iteratee.{
  Cont,
  Done,
  Error,
  Enumerator => PlayEnum,
  Input,
  Iteratee => PlayIter,
  Step
}
import play.api.libs.iteratee.Execution.{ defaultExecutionContext => dec }


object Enumerator {

  def enumerator(resultSet: ResultSet)(implicit ctx: scala.concurrent.ExecutionContext): PlayEnum[Row] = {

    new PlayEnum[Row] {

      val rs = resultSet
      val it = rs.iterator

      def apply[A](iter: PlayIter[Row, A]): Future[PlayIter[Row, A]] = {

        def step(iter: PlayIter[Row, A]): Future[PlayIter[Row, A]] = {
          iter.fold {
            case Step.Cont(k) => fetch[A](step, k)
            case Step.Done(a, e) => Future.successful(Done(a, e))
            case Step.Error(msg, inp) => Future.successful(Error(msg, inp))
          }(dec)
        }

        step(iter)
      }

      def fetch[A](loop: PlayIter[Row, A] => Future[PlayIter[Row, A]], k: Input[Row] => PlayIter[Row, A]): Future[PlayIter[Row, A]] = {

        val available = resultSet.getAvailableWithoutFetching

        if (!rs.isExhausted) {
          // prefetch if we are running low on results.
          if (available < 100 && !resultSet.isFullyFetched)
            rs.fetchMoreResults
          // if we have less than one result available non-blocking
          // we send the next invocation of the iterator to the thread
          // pool as blocking
          if (available < 1) {
            Future(it.next)(ctx).flatMap(row => loop(k(Input.El(row))))(dec)
          }
          // otherwise we can just carry on.
          else {
            loop(k(Input.El(it.next)))
          }
        } else {
          Future.successful(Cont(k))
        }
      }
    }
  }
}

