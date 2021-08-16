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
import shapeless.Generic

import scala.annotation.implicitNotFound
import scala.reflect.macros.whitebox

/**
  * A very dirty typeclass used to duct type single argument calls to the store varargs autotupled method.
  * We currently expect the store method to auto-tuple argument types, which leads to problems when a single
  * tuple argument is passed. In order to fix this, this type class will effectively pick a matching type
  * between our single implementation of [[SingleGeneric]] and the traditional [[shapeless.Generic]].
  *
  * Unlike the traditional implementation, a single tuple argument must not be expanded into a [[shapeless.HList]] of its
  * enclosing types, but rather an [[shapeless.HList]] that merely wraps around the source type.
  *
  * Example: {{{
  *   val singleGeneric = SingleGeneric[(String, Int)].Repr
  *   (String, Int) :: HNil
  *
  *   val shapelessGeneric = Generic[(String, Int)]
  *   String :: Int :: HNil
  * }}}
  *
  * Unlike the traditional implementation of [[Generic]] we don't always need to destructure tuples, but because
  * we cannot effectively distinguish, we are forced to duct-type and pick the appropriate implementation
  * based on which variation between [[GenR]] and [[Store]] matches the input type.
  *
  * @tparam T The source type of the record that is passed to the store method as an auto-tupling argument.
  * @tparam Store The HList input type inferred by [[TableHelper]].
  * @tparam GenR The generic HList type inferred by [[shapeless.Generic]].
  */
@implicitNotFound(msg = "The type you're trying to store ${T} should match either ${Store} or ${GenR}")
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


class SingleGenericMacro(val c: whitebox.Context) extends HListHelpers with WhiteboxToolbelt {
  import c.universe._

  protected[this] val macroPkg = q"_root_.com.outworkers.phantom.macros"
  private[this] def genericTpe(tpe: Type): Tree = tq"_root_.shapeless.Generic[$tpe]"

  def mkGeneric(tpe: Type, store: Type, generic: Type): Tree = {
    val res = mkHListType(tpe :: Nil)
    val genTpe = genericTpe(tpe)

    val tree = if (store =:= generic) {
      info(s"Generic implementation using Shapeless for ${printType(tpe)}")
      q"""
          new $macroPkg.SingleGeneric[$tpe, $store, $generic] {
            type Repr = $generic

            def to(source: $tpe)(implicit gen: ${genericTpe(tpe)}): $generic = {
              (gen to source).asInstanceOf[$generic]
            }

            def from(hl: $generic)(implicit gen: $genTpe): $tpe = (gen from hl.asInstanceOf[gen.Repr])
          }
      """
    } else if (store =:= res) {
      info(s"Single generic implementation using coalesced HLists for ${printType(tpe)}")

      q"""
          new $macroPkg.SingleGeneric[$tpe, $store, $generic] {
            type Repr = $res

            def to(source: $tpe)(implicit gen: $genTpe): $res = source :: _root_.shapeless.HNil

            def from(hl: $res)(implicit gen: $genTpe): $tpe = hl.apply(_root_.shapeless.Nat._0)
          }
      """
    } else {
      val debugString = s"Unable to derive store type for ${printType(tpe)}, expected ${showHList(generic)} or ${showHList(store)}"
      error(debugString)
      abort(debugString)
    }

    evalTree(tree)
  }

  def materialize[T : c.WeakTypeTag, Store : c.WeakTypeTag, HL : c.WeakTypeTag]: Tree = {
    val tableType = weakTypeOf[T]
    val store = weakTypeOf[Store]
    val generic = weakTypeOf[HL]


    memoize[(Type, Type, Type), Tree](
      WhiteboxToolbelt.singeGenericCache
    )(Tuple3(tableType, store, generic), { case (t, s, g) =>
      mkGeneric(t, s, g)
    })
  }
}
