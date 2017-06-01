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

import shapeless.Generic

import scala.reflect.macros.whitebox

trait SingleGeneric[T, Store, GenR] extends Serializable {
  /** The generic representation type for {T}, which will be composed of {Coproduct} and {HList} types  */
  type Repr

  /** Convert an instance of the concrete type to the generic value representation */
  def to(t: T)(implicit gen: Generic[T]) : Repr

  /** Convert an instance of the generic representation to an instance of the concrete type */
  def from(r: Repr)(implicit gen: Generic[T]) : T
}

object SingleGeneric {

  def apply[T, Store, HL](implicit ev: SingleGeneric[T, Store, HL]): SingleGeneric[T, Store, HL] = ev

  implicit def materialize[
    T,
    Store,
    HL
  ]: SingleGeneric[T, Store, HL] = macro SingleGenericMacro.materialize[T, Store, HL]

  type Aux[T, Store, HL, Repr0] = SingleGeneric[T, Store, HL] { type Repr = Repr0 }
}


@macrocompat.bundle
class SingleGenericMacro(val c: whitebox.Context) extends HListHelpers with WhiteboxToolbelt {
  import c.universe._

  protected[this] val macroPkg = q"_root_.com.outworkers.phantom.macros"
  private[this] def genericTpe(tpe: Type): Tree = tq"_root_.shapeless.Generic[$tpe]"

  def mkGeneric(tpe: Type, store: Type, generic: Type): Tree = {
    val res = mkHListType(tpe :: Nil)
    val genTpe = genericTpe(tpe)

    val tree = if (store =:= generic) {
      q"""
          new $macroPkg.SingleGeneric[$tpe, $store, $generic] {
            type Repr = $generic

            def to(source: $tpe)(implicit gen: $genTpe): $generic = gen to source

            def from(hl: $generic)(implicit gen: $genTpe): $tpe = gen from hl
          }
      """
    } else if (store =:= res) {
      q"""
          new $macroPkg.SingleGeneric[$tpe, $store, $generic] {
            type Repr = $res

            def to(source: $tpe)(implicit gen: $genTpe): $res = source :: _root_.shapeless.HNil

            def from(hl: $res)(implicit gen: $genTpe): $tpe = hl.apply(_root_.shapeless.Nat._0)
          }
      """
    } else {
      c.abort(c.enclosingPosition, s"Unable to derive store type for ${printType(tpe)}")
    }

    Console.println(showCode(tree))

    if (showTrees) {
      c.echo(c.enclosingPosition, showCode(tree))
    }
    tree
  }

  def materialize[T : c.WeakTypeTag, Store : c.WeakTypeTag, HL : c.WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    val store = weakTypeOf[Store]
    val generic = weakTypeOf[HL]

    memoize[(Type, Type, Type), Tree](
      WhiteboxToolbelt.singeGenericCache
    )(Tuple3(tpe, store,generic), { case (t, s, g) => mkGeneric(t, s, g)})
  }
}