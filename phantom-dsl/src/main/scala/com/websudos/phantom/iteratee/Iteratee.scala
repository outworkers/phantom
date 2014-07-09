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

import scala.collection.immutable.Queue
import scala.concurrent.ExecutionContext
import play.api.libs.iteratee.{ Iteratee => PIteratee }

/**
 * Helper object to some commune use cases for iteratee
 * This is a wrapper around play Iteratee class
 */
object Iteratee {
  def collect[R]()(implicit ec: ExecutionContext): PIteratee[R, Queue[R]] =
    PIteratee.fold(Queue.empty[R])((acc, e: R)=> acc :+ e)

  def chunks[R]()(implicit ec: ExecutionContext): PIteratee[R, List[R]] = {
    PIteratee.getChunks
  }

  def forEach[E](f: E => Unit)(implicit ec: ExecutionContext): PIteratee[E, Unit] = PIteratee.foreach(f: E => Unit)

  def drop[R](num: Int)(implicit ex: ExecutionContext): PIteratee[R, Iterator[R]] = {
    PIteratee.fold(Iterator[R]())((acc: Iterator[R], e: R) => acc ++ Iterator(e) ) map (_.drop(num))
  }

  def slice[R](start: Int, limit: Int)(implicit ex: ExecutionContext): PIteratee[R, Iterator[R]] = {
    PIteratee.fold(Iterator[R]())((acc: Iterator[R], e: R) => acc ++ Iterator(e) ) map (_.slice(start, start + limit))
  }

  def take[R](limit: Int)(implicit ex: ExecutionContext): PIteratee[R, Iterator[R]] = {
    PIteratee.fold(Iterator[R]())((acc: Iterator[R], e: R) => acc ++ Iterator(e) ) map (_.take(limit))
  }
}
