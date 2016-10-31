/*
 * Copyright 2013-2017 Outworkers, Limited.
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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.outworkers.phantom.builder.ops

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.column.AbstractColumn
import shapeless._

object TokenTypes {
  sealed trait Root
  trait ValueToken extends Root
  trait ColumnToken extends Root

}

sealed trait TokenValueApplyOps {

  def apply[R1, R2, VL <: HList](value: R1, value2: R2)(
    implicit ev: Primitive[R1],
    ev2: Primitive[R2],
    gen: Generic.Aux[(R1, R2), VL]
  ): TokenConstructor[VL, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(ev.asCql(value), ev2.asCql(value2)))
  }

  def apply[R1, R2, R3, VL <: HList](value: R1, value2: R2, value3: R3)(
    implicit ev: Primitive[R1],
    ev2: Primitive[R2],
    ev3: Primitive[R3],
    gen: Generic.Aux[(R1, R2, R3), VL]
  ): TokenConstructor[VL, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(ev.asCql(value), ev2.asCql(value2), ev3.asCql(value3)))
  }

  def apply[R1, R2, R3, R4, VL <: HList](value: R1, value2: R2, value3: R3, value4: R4)(
    implicit ev: Primitive[R1],
    ev2: Primitive[R2],
    ev3: Primitive[R3],
    ev4: Primitive[R4],
    gen: Generic.Aux[(R1, R2, R3, R4), VL]
  ): TokenConstructor[VL, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(ev.asCql(value), ev2.asCql(value2), ev3.asCql(value3), ev4.asCql(value4)))
  }
}

sealed trait TokenColumnApplyOps {

  def apply[
    X1 <: AbstractColumn[_],
    X2 <: AbstractColumn[_],
    VL <: HList
  ](value: X1, value2: X2)(
    implicit gen: Generic.Aux[(X1#Value, X2#Value), VL]): TokenConstructor[VL, TokenTypes.ColumnToken] = {
    new TokenConstructor(Seq(value.name, value2.name))
  }

  def apply[
    X1 <: AbstractColumn[_],
    X2 <: AbstractColumn[_],
    X3 <: AbstractColumn[_],
    VL <: HList
  ](value: X1, value2: X2, value3: X3)(
    implicit gen: Generic.Aux[(X1#Value, X2#Value, X3#Value), VL]): TokenConstructor[VL, TokenTypes.ColumnToken] = {
    new TokenConstructor(Seq(value.name, value2.name, value3.name))
  }

  def apply[
    X1 <: AbstractColumn[_],
    X2 <: AbstractColumn[_],
    X3 <: AbstractColumn[_],
    X4 <: AbstractColumn[_],
    VL <: HList
  ](value: X1, value2: X2, value3: X3, value4: X4)(
    implicit gen: Generic.Aux[(X1#Value, X2#Value, X3#Value, X4#Value), VL]): TokenConstructor[VL, TokenTypes.ColumnToken] = {
    new TokenConstructor(Seq(value.name, value2.name, value3.name, value4.name))
  }
}


trait TokenComparisonOps extends TokenColumnApplyOps with TokenValueApplyOps