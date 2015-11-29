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
package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.column.AbstractColumn
import shapeless.{Generic, HList, ::, HNil}

sealed trait TokenValueApplyOps {

  def apply[R1, VL <: HList](value: R1)(
    implicit ev: Primitive[R1],
    gen: Generic.Aux[Tuple1[R1], VL]
  ): TokenConstructor[VL] = {
    new TokenConstructor[VL](Seq(ev.asCql(value)))
  }

  def apply[R1, R2, VL <: HList](value: R1, value2: R2)(
    implicit ev: Primitive[R1],
    ev2: Primitive[R2],
    gen: Generic.Aux[(R1, R2), VL]
  ): TokenConstructor[VL] = {
    new TokenConstructor[VL](Seq(ev.asCql(value), ev2.asCql(value2)))
  }

  def apply[R1, R2, R3, VL <: HList](value: R1, value2: R2, value3: R3)(
    implicit ev: Primitive[R1],
    ev2: Primitive[R2],
    ev3: Primitive[R3],
    gen: Generic.Aux[(R1, R2, R3), VL]
  ): TokenConstructor[VL] = {
    new TokenConstructor[VL](Seq(ev.asCql(value), ev2.asCql(value2), ev3.asCql(value3)))
  }
}

sealed trait TokenColumnApplyOps {

  def apply[V1 <: AbstractColumn[R1], R1](value: V1): TokenConstructor[R1 :: HNil] = {
    new TokenConstructor[R1 :: HNil](Seq(value.name))
  }

  def apply[X1 <: AbstractColumn[_], X2 <: AbstractColumn[_], VL <: HList](value: X1, value2: X2)(
    implicit gen: Generic.Aux[(X1#Value, X2#Value), VL]): TokenConstructor[VL] = {
    new TokenConstructor[VL](Seq(value.name, value2.name))
  }

  def apply[X1 <: AbstractColumn[_], X2 <: AbstractColumn[_], X3 <: AbstractColumn[_], VL <: HList](value: X1, value2: X2, value3: X3)(
    implicit gen: Generic.Aux[(X1#Value, X2#Value, X3#Value), VL]): TokenConstructor[VL] = {
    new TokenConstructor[VL](Seq(value.name, value2.name, value3.name))
  }
}


trait TokenComparisonOps extends TokenValueApplyOps with TokenColumnApplyOps