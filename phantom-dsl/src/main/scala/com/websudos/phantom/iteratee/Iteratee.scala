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

import play.api.libs.iteratee.{Iteratee => PIteratee}

import scala.concurrent.ExecutionContext

/**
 * Helper object to some common use cases for iterators.
 * This is a wrapper around play Iteratee class.
 */
object Iteratee {
  def collect[R]()(implicit ec: ExecutionContext): PIteratee[R, List[R]] =
    PIteratee.fold(List.empty[R])((acc, e: R)=> e :: acc)

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
