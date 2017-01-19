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
package com.outworkers.phantom.streams.iteratee

import play.api.libs.iteratee.{Iteratee => PlayIteratee}

import scala.concurrent.{ExecutionContext, Future}

case class Wrapper[R](iterator: Iterator[R], limit: Int) {
  def add(itt: Iterator[R], offset: Int): Wrapper[R] = {
    Wrapper(iterator ++ itt, limit + offset)
  }
}

object Wrapper {
  def empty[R]: Wrapper[R] = Wrapper(Iterator[R](), 0)
}

/**
 * Helper object to some common use cases for iterators.
 * This is a wrapper around play Iteratee class.
 */
object Iteratee {
  def collect[R]()(implicit ec: ExecutionContext): PlayIteratee[R, List[R]] =
    PlayIteratee.fold(List.empty[R])((acc, e: R)=> e :: acc)

  def chunks[R]()(implicit ec: ExecutionContext): PlayIteratee[R, List[R]] = {
    PlayIteratee.getChunks
  }

  /**
   * Counts the number of elements inside the iteratee using a fold traversal.
   * @param f The function to use for counting the records, takes a record as input.
   * @param ec The execution context in which to execute the operation.
   * @tparam E The type of the Record, dictated by the Cassandra table.
   * @return A new iteratee, where the result of the operation is a long with the count.
   */
  def count[E](f: E => Long)(implicit ec: ExecutionContext): PlayIteratee[E, Long] = {
    PlayIteratee.fold(0L)((acc, _) => acc + 1)
  }

  /**
    * Executes a function for every single element in the iteratee.
    * @param f The function to execute.
    * @param ec The execution context to execute the operation in.
    * @tparam E The type of the underlying record, dictated by the Cassandra table.
    * @return A new iteratee, with the result type Unit.
    */
  def forEach[E](f: E => Unit)(implicit ec: ExecutionContext): PlayIteratee[E, Unit] = {
    PlayIteratee.foreach(f: E => Unit)
  }

  /**
    * A drop method called directly on the iteratee, will asynchronously process the drop using the Play Iteratee API.
    * @param num The number of records to drop from the "left hand side" of the iteratee.
    * @param ex The execution context in which to execute the operation.
    * @tparam R The type of the Record being selected from the Cassandra table.
    * @return A new iteratee, where the first num records have been dropped.
    */
  def drop[R](num: Int)(implicit ex: ExecutionContext): PlayIteratee[R, Iterator[R]] = {
    PlayIteratee.fold2(Wrapper.empty[R])((wrapper: Wrapper[R], el: R) =>
      if (wrapper.limit >= num) {
        Future.successful(Tuple2(wrapper add (Iterator(el), 1), false))
      } else {
        Future.successful(Tuple2(wrapper, true))
      }
    ) map (_.iterator)
  }

  /**
    * Slices the iteratee from a given index to as many elements as the limit, effectively returns all elements from
    * (start) to (start + limit).
    *
    * @param start The index at which to start.
    * @param limit The number of elements to include in the slice.
    * @param ex The execution context in which to perform the computation.
    * @tparam R The type of the result to return in the iterator, usually the record type of a table.
    * @return A Play iteratee that can be consumed.
    */
  def slice[R](start: Int, limit: Int)(implicit ex: ExecutionContext): PlayIteratee[R, Iterator[R]] = {
    PlayIteratee.fold2(Wrapper.empty[R])((wrapper: Wrapper[R], el: R) =>
      // If we are in the target (start -> (start + limit)) interval add elements to the accumulator.
      if (wrapper.limit >= start && wrapper.limit < start + limit) {
        Future.successful(Tuple2(wrapper add (Iterator(el), 1), false))
      } else if (wrapper.limit >= (start + limit)) {
        Future.successful(Tuple2(wrapper, true))
      } else {
        Future.successful(Tuple2(wrapper, false))
      }
    ) map (_.iterator)
  }

  def take[R](limit: Int)(implicit ex: ExecutionContext): PlayIteratee[R, Iterator[R]] = {
    PlayIteratee.fold2(Wrapper.empty[R])((wrapper: Wrapper[R], el: R) =>
      if (wrapper.limit < limit) {
        Future.successful(Tuple2(wrapper add (Iterator(el), 1), false))
      } else {
        Future.successful(Tuple2(wrapper, true))
      }
    ) map (_.iterator)
  }
}
