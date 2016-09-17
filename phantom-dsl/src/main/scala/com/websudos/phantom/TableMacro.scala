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
package com.websudos.phantom

import com.datastax.driver.core.Row
import com.websudos.phantom.column.AbstractColumn

import scala.language.experimental.macros
import scala.reflect.macros.blackbox

private[phantom] object TableMacro {

  def tableName[T : c.WeakTypeTag](c: blackbox.Context): c.Expr[String] = {
    import c.universe._
    val tpe = weakTypeOf[T]
    c.Expr[String](q"""${tpe.termSymbol.asTerm.name}""")
  }

  def fieldList[T : c.WeakTypeTag](c: blackbox.Context): c.Expr[Seq[AbstractColumn[_]]] = {
    import c.universe._

    val tpe = weakTypeOf[T]
    val accessors: Iterable[TermName] = tpe.decls
      .filter(sym => sym.isModule && sym.asMethod.typeSignature <:< typeOf[AbstractColumn[_]])
      .map(_.asTerm.name)

    val res = q"""Seq(..$accessors)"""

    c.Expr[Seq[AbstractColumn[_]]](res)
  }

  def fields(c: blackbox.Context)(tpe: c.universe.Type): Iterable[(c.universe.Name, c.universe.Type)] = {
    import c.universe._

    object CaseField {
      def unapply(arg: TermSymbol): Option[(Name, Type)] = {
        if (arg.isVal && arg.isCaseAccessor) {
          Some(TermName(arg.name.toString.trim), arg.typeSignature)
        } else {
          None
        }
      }
    }

    tpe.decls.collect { case CaseField(name, fType) => name -> fType }
  }

  def fromRowMacroImpl[
    Table : c.WeakTypeTag,
    Res : c.WeakTypeTag
  ](c: blackbox.Context)(r: c.Expr[Row]): c.Expr[Res] = {
    import c.universe._

    val tpe: Type = weakTypeOf[Table]
    val resType: Type = weakTypeOf[Res]

    val companion = resType.typeSymbol.companion

    val accessors = tpe.decls
      .filter(sym => sym.isModule && sym.asMethod.typeSignature <:< typeOf[AbstractColumn[_]])

    val extractors = accessors map {
      symbol => q"""Primitive[Int].fromRow(${symbol.name.toTermName}.name, row).get"""
    }

    val res = q"""$companion.apply(..$extractors)"""

    println(showCode(res))

    c.Expr(res)
  }
}
