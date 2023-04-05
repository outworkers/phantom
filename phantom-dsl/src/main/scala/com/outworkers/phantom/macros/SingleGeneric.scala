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

import shapeless.{::, Generic, HNil}

import scala.annotation.implicitNotFound

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
  type Repr = Store

  /** Convert an instance of the concrete type to the generic value representation */
  def to(t: T) : Repr

  /** Convert an instance of the generic representation to an instance of the concrete type */
  def from(r: Repr) : T
}

trait LowPrioritySingleGeneric {
  implicit def single[T, HL](implicit gen: Generic.Aux[T, HL]): SingleGeneric[T, T :: HNil, HL] =
    new SingleGeneric[T, T :: HNil, HL] {
      def to(source: T): T :: HNil = source :: HNil

      def from(hl: T :: HNil): T = hl.head
    }
}

object SingleGeneric extends LowPrioritySingleGeneric {

  def apply[T, Store, HL](implicit ev: SingleGeneric[T, Store, HL]): SingleGeneric[T, Store, HL] = ev

  implicit def generic[T, HL](implicit gen: Generic.Aux[T, HL]): SingleGeneric[T, HL, HL] =
    new SingleGeneric[T, HL, HL] {
      def to(source: T): HL = gen to source

      def from(hl: HL): T = gen from hl
    }
}