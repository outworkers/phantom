/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

import com.outworkers.phantom.macros.toolbelt.{HListHelpers, WhiteboxToolbelt}
import shapeless.HList

import scala.annotation.implicitNotFound
import scala.reflect.macros.whitebox

/**
  * A special implicitly materialized typeclass that compares HLists for equality.
  * This also prints more useful information to the end user if the HLists don't match.
  * However, it is required because the standard [[=:=]] type class in Scala
  * is not able to see through an HList generated as an abstract type member of another
  * implicitly materialized type class.
  * @tparam LL The left [[shapeless.HList]] type.
  * @tparam RR The right [[shapeless.HList]] type.
  */
@implicitNotFound(msg = "HList ${LL} does not contain the same types as ${RR}")
trait ==:==[LL <: HList, RR <: HList]

object ==:== {

  def apply[LL <: HList, RR <: HList](implicit ev: LL ==:== RR): ==:==[LL, RR] = ev

  implicit def materialize[
    LL <: HList,
    RR <: HList
  ]: ==:==[LL, RR] = macro EqsMacro.materialize[LL, RR]
}


class EqsMacro(val c: whitebox.Context) extends WhiteboxToolbelt with HListHelpers {

  import c.universe._

  protected[this] val macroPkg = q"_root_.com.outworkers.phantom.macros"


  def mkEqs(left: Type, right: Type): Tree = {
    if (left =:= right) {
      evalTree {
        q"""new $macroPkg.==:==[$left, $right] {}"""
      }

    } else {
      val debugString = s"Types ${showHList(left)} did not equal ${showHList(right)}"
      error(debugString)
      abort( debugString)
    }
  }

  def materialize[LL <: HList : c.WeakTypeTag, RR <: HList : c.WeakTypeTag]: Tree = {
    val left = weakTypeOf[LL]
    val right = weakTypeOf[RR]

    memoize[(Type, Type), Tree](
      WhiteboxToolbelt.specialEqsCache
    )(Tuple2(left, right), { case (t, s) => mkEqs(t, s)})
  }


}
