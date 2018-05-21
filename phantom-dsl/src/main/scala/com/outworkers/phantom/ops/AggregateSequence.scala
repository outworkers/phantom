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

import shapeless._
import shapeless.ops.tuple.Prepend

/*
trait AggregateSequence[L <: HList] extends DepFn1[L]

object AggregateSequence {
  type Aux[L <: HList, Out0] = AggregateSequence[L] { type Out = Out0 }

  implicit def hnilAggregateSequence: Aux[HNil, Option[Unit]] =
    new AggregateSequence[HNil] {
      type Out = Option[Unit]
      def apply(l: HNil): Option[Unit] = Some(())
    }

  implicit def hconsAggregateSequence[H, T <: HList, OutT](
    implicit fst: Aux[T, Option[OutT]],
    pre: Prepend[Tuple1[H], OutT]
  ): Aux[Option[H] :: T, Option[pre.Out]] = new AggregateSequence[Option[H] :: T] {
    type Out = Option[pre.Out]

    def apply(l: Option[H] :: T): Option[pre.Out] = {
      l.head.flatMap(fst(l.tail)).map {
        case (h, t) => pre(Tuple1(h), t)
      }
    }
  }

}*/