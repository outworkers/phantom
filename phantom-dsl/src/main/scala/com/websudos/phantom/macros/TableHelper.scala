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
package com.websudos.phantom.macros

import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.dsl.CassandraTable
import com.datastax.driver.core.Row

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  def fromRow(row: Row): R

  def fields: Seq[AbstractColumn[_]]
}

object TableHelper {
  implicit def fieldsMacro[T]: Seq[AbstractColumn[_]] = macro TableHelperMacro.macroImpl[T]
}

@macrocompat.bundle
class TableHelperMacro(val c: blackbox.Context) {

  import c.universe._

  def macroImpl[T : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    q"""
       new com.websudos.phantom.macros.TableHelper[$tpe] {

       }
     """
  }


  def tableName[T : WeakTypeTag]: Expr[String] = {
    val tpe = weakTypeOf[T]
    c.Expr[String](q"""${tpe.termSymbol.asTerm.name}""")
  }

  def fieldList[T : WeakTypeTag]: c.Expr[Seq[AbstractColumn[_]]] = {
    val tpe = weakTypeOf[T]
    val accessors: Iterable[TermName] = tpe.decls
      .filter(sym => sym.isModule && sym.asMethod.typeSignature <:< typeOf[AbstractColumn[_]])
      .map(_.asTerm.name)

    val res = q"""Seq(..$accessors)"""

    c.Expr[Seq[AbstractColumn[_]]](res)
  }

  def fields(tpe: Type): Iterable[(Name, Type)] = {
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

  def fieldsMacroImpl[T : WeakTypeTag]: Expr[Seq[AbstractColumn[_]]] = {

    val tpe = weakTypeOf[T]
    val accessors: Iterable[TermName] = tpe.decls
      .filter(sym => sym.isModule && sym.asMethod.typeSignature <:< typeOf[AbstractColumn[_]])
      .map(_.asTerm.name)

    val res = q"""Seq(..$accessors)"""

    c.Expr[Seq[AbstractColumn[_]]](res)
  }
}