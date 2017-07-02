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
package com.outworkers.phantom.builder.ops

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.column.AbstractColumn
import shapeless._

object TokenTypes {
  sealed trait Root
  trait ValueToken extends Root
  trait ColumnToken extends Root

}

sealed trait TokenValueApplyOps {

  def apply[R1](value: R1)(
    implicit ev: Primitive[R1]
  ): TokenConstructor[R1 :: HNil, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(ev.asCql(value)))
  }

  def apply[RR](value: PrepareMark)(
  ): TokenConstructor[RR :: HNil, TokenTypes.ValueToken] = {
    new TokenConstructor(Seq(value.qb.queryString))
  }

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
    X1 <: AbstractColumn[_]
  ](value: X1): TokenConstructor[X1#Value :: HNil, TokenTypes.ColumnToken] = {
    new TokenConstructor(Seq(value.name))
  }

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