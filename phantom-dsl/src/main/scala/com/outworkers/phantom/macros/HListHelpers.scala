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
package com.outworkers.phantom.macros

import shapeless.{HList, HNil}

import scala.reflect.macros.whitebox

@macrocompat.bundle
trait HListHelpers {
  val c: whitebox.Context

  import c.universe._

  protected[this] def hlistTpe: Type = typeOf[HList]
  protected[this] def hnilTpe: Type = typeOf[HNil]
  protected[this] def hconsTpe: Type = typeOf[shapeless.::[_, _]].typeConstructor

  def printType(tpe: Type): String = showCode(tq"${tpe.dealias}")

  def showCollection[
    M[X] <: TraversableOnce[X]
  ](traversable: M[Type], sep: String = ", "): String = {
    traversable map printType mkString sep
  }

  def isVararg(tpe: Type): Boolean =
    tpe.typeSymbol == c.universe.definitions.RepeatedParamClass

  def devarargify(tpe: Type): Type =
    tpe match {
      case TypeRef(pre, _, args) if isVararg(tpe) =>
        appliedType(typeOf[scala.collection.Seq[_]].typeConstructor, args)
      case _ => tpe
    }

  def mkCompoundTpe(nil: Type, cons: Type, items: List[Type]): Type = {
    items.foldRight(nil) {
      case (tpe, acc) => appliedType(cons, List(devarargify(tpe), acc))
    }
  }

  def mkHListType(col: List[Type]): Type = mkCompoundTpe(hnilTpe, hconsTpe, col)

}