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
import scala.reflect.macros.blackbox

@macrocompat.bundle
class MacroUtils(val c: blackbox.Context) {
  import c.universe._

  def fields(tpe: Type): Iterable[(Name, Type)] = {
    object CaseField {
      def unapply(arg: TermSymbol): Option[(Name, Type)] = {
        if (arg.isVal && arg.isCaseAccessor) {
          Some(TermName(arg.name.toString.trim) -> arg.typeSignature)
        } else {
          None
        }
      }
    }

    tpe.decls.collect { case CaseField(name, fType) => name -> fType }
  }

  def baseType(sym: Symbol): Set[Symbol] = {
    if (sym.isClass) {
      val clz = sym.asClass
      val base = clz.baseClasses.toSet - clz //`baseClasses` contains `a` itself
      val basebase = base.flatMap {
        case x: ClassSymbol => x.baseClasses.toSet - x
      }
      base -- basebase
    } else {
      c.abort(c.enclosingPosition, s"Expected a class, but the symbol was different")
    }
  }

  def filterMembers[Filter : TypeTag](
    tag: Type,
    exclusions: Symbol => Option[Symbol]
  ): Seq[Symbol] = {
    val tpe = tag.typeSymbol.typeSignature

    (
      for {
        baseClass <- tpe.baseClasses.reverse.flatMap(exclusions(_))
        symbol <- baseClass.typeSignature.members.sorted
        if symbol.typeSignature <:< typeOf[Filter]
      } yield symbol
      )(collection.breakOut) distinct
  }

  def filterMembers[T : WeakTypeTag, Filter : TypeTag](
    exclusions: Symbol => Option[Symbol] = { s: Symbol => Some(s) }
  ): Seq[Symbol] = {
    val tpe = weakTypeOf[T].typeSymbol.typeSignature

    (
      for {
        baseClass <- tpe.baseClasses.reverse.flatMap(exclusions(_))
        symbol <- baseClass.typeSignature.members.sorted
        if symbol.typeSignature <:< typeOf[Filter]
      } yield symbol
    )(collection.breakOut) distinct
  }
}
