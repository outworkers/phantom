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

import scala.annotation.tailrec
import scala.reflect.macros.whitebox

trait ==:==[Left <: HList, Right <: HList]

object ==:== {

  def apply[Left <: HList, Right <: HList](implicit ev: Left ==:== Right): ==:==[Left, Right] = ev

  implicit def materialize[
    Left <: HList, Right <: HList
  ]: ==:==[Left, Right] = macro EqsMacro.materialize[Left, Right]
}


@macrocompat.bundle
class EqsMacro(val c: whitebox.Context) {

  import c.universe._

  private[this] def hlistTpe = typeOf[HList]
  private[this] def hnilTpe = typeOf[HNil]
  private[this] def hconsTpe = typeOf[shapeless.::[_, _]].typeConstructor

  protected[this] val macroPkg = q"_root_.com.outworkers.phantom.macros"

  def prefix(tpe: Type): Type = {
    val global = c.universe.asInstanceOf[scala.tools.nsc.Global]
    val gTpe = tpe.asInstanceOf[global.Type]
    gTpe.prefix.asInstanceOf[Type]
  }

  def showCollection[
    M[X] <: TraversableOnce[X]
  ](traversable: M[Type], sep: String = ", "): String = {
    traversable map printType mkString ("[", sep, "]")
  }

  def unpackHListTpe(tpe: Type): List[Type] = {
    @tailrec
    def unfold(u: Type, acc: List[Type]): List[Type] = {
      val HNilTpe = hnilTpe
      val HConsPre = prefix(hconsTpe)
      val HConsSym = hconsTpe.typeSymbol
      if(u <:< HNilTpe) {
        acc
      } else {
        u baseType HConsSym match {
          case TypeRef(pre, _, List(hd, tl)) if pre =:= HConsPre => unfold(tl, hd :: acc)
          case _ => c.abort(c.enclosingPosition, s"$tpe is not an HList type")
        }
      }
    }

    unfold(tpe, List()).reverse
  }

  def printType(tpe: Type): String = showCode(tq"${tpe.dealias}")

  def materialize[Left <: HList : c.WeakTypeTag, Right <: HList : c.WeakTypeTag]: Tree = {

    val left = weakTypeOf[Left]
    val right = weakTypeOf[Right]

    if (left =:= right) {
      q"""new $macroPkg.==:==[$left, $right] {}"""
    } else {
      val leftUnpacked = unpackHListTpe(left)
      val rightUnpacked = unpackHListTpe(right)
      val debugString = s"Types ${showCollection(leftUnpacked)} did not equal ${showCollection(rightUnpacked)}"
      Console.println(debugString)
      c.info(c.enclosingPosition, debugString, force = true)
      c.abort(c.enclosingPosition, debugString)
    }
  }


}
