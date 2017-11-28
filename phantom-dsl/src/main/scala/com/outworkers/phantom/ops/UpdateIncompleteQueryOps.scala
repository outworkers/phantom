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
package com.outworkers.phantom.ops

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.query.SetPart
import com.outworkers.phantom.builder.query.execution._

import scala.concurrent.ExecutionContextExecutor

class UpdateIncompleteQueryOps[
  P[_],
  F[_]
](
  query: ExecutableCqlQuery,
  setPart: SetPart
)(
  implicit pf: PromiseInterface[P, F],
  fMonad: FutureMonad[F]
) {

  /**
    * A method used to allow ignoring unset UPDATE clauses that can sometimes happen
    * when the {{$setIfDefined}} operator is used. To allow updating arbitrary records, phantom has a
    * way to allow ignoring fields from being set in an update clause if they are of type scala.None.
    * @return A Future of Unit, as the result here cannot be known. If the update clause is invalid because
    *         the set part of the query is incomplete at the point where this method is invoked, the result
    *         is a successful future of Unit.
    */
  def succeedAnyway()(
    implicit session: Session,
    ctx: ExecutionContextExecutor
  ): F[Unit] = {
    if (setPart.nonEmpty) {
      fMonad.map(pf.adapter.fromGuava(query))(_ => ())
    } else {
      pf.apply(())
    }
  }
}
