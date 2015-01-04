/*
 * Copyright 2013 websudos ltd.
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

