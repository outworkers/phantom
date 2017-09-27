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
package com.outworkers.phantom.builder.query.execution

import scala.concurrent.ExecutionContextExecutor

/**
  * A special mini monad implementation that allows us not to have to rely on Cats being
  * part of our dependencies as it's really heavy to use for this simple reason.
  * Cats also requires binding a future monad for Scala futures against a known [[ExecutionContextExecutor]],
  * which unfortunately interferes with one of the core features in phantom, giving users the ability
  * to supply their own execution context whenever they want.
  *
  * In our implementation, we go against the grain and include the implicit [[ExecutionContextExecutor]] as part
  * of the standard method signature for map and flatMap, and this is only because we want to keep enabling users
  * to supply their own context. Certain implementations of Futures will downright ignore this implicit parameter,
  * but it's there for the implementations of Future and Promise that need it, specifically Scala Standard Lib
  * [[scala.concurrent.Future]] and [[scala.concurrent.Promise]].
  *
  * Cats is a much better library in any conceivable way, and while we are not trying to reinvent category theory,
  * it's too heavy of a dependency to add for the sake of a single simple typeclass.
  *
  * @tparam F The type of the Future implementation for which we implement map and flatMap.
  */
trait FutureMonad[F[_]] {

  def pure[A](source: A): F[A]

  def map[A, B](source: F[A])(f: A => B)(
    implicit ctx: ExecutionContextExecutor
  ): F[B]

  def flatMap[A, B](source: F[A])(fn: A => F[B])(
    implicit ctx: ExecutionContextExecutor
  ): F[B]
}


object FutureMonadOps {
  implicit class Ops[F[_], A](val source: F[A])(implicit monad: FutureMonad[F]) {
    def map[B](f: A => B)(
      implicit ctx: ExecutionContextExecutor
    ): F[B] = monad.map(source)(f)

    def flatMap[B](fn: A => F[B])(
      implicit ctx: ExecutionContextExecutor
    ): F[B]= monad.flatMap(source)(fn)
  }
}