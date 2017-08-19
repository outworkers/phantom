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
package com.outworkers.phantom.builder.query

import scala.concurrent.ExecutionContextExecutor

package object execution {

  implicit class FunctorOps[F[_], T](val in: F[T])(implicit fMonad: FutureMonad[F]) {
    def zipWith[O, R](other: F[O])(fn: (T, O) => R)(
      implicit ctx: ExecutionContextExecutor
    ): F[R] = {
      for (r1 <- in; r2 <- other) yield fn(r1, r2)
    }

    def map[A](fn: T => A)(
      implicit ctx: ExecutionContextExecutor
    ): F[A] = fMonad.map(in)(fn)

    def flatMap[A](fn: T => F[A])(
      implicit ctx: ExecutionContextExecutor
    ): F[A] = fMonad.flatMap(in)(fn)
  }
}