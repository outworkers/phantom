/*
 * Copyright 2013-2017 Outworkers, Limited.
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
package com.outworkers.phantom.macros

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.database.{Database, ExecutableCreateStatementsList}
import com.outworkers.phantom.builder.query.CreateQuery
import com.outworkers.phantom.connectors.KeySpace

import scala.reflect.macros.blackbox

trait DatabaseHelper[T <: Database[T]] {
  def tables(db: T): Set[CassandraTable[_ ,_]]

  def createQueries(db: T)(implicit keySpace: KeySpace): ExecutableCreateStatementsList
}

object DatabaseHelper {
  implicit def macroMaterialise[T <: Database[T]]: DatabaseHelper[T] = macro DatabaseHelperMacro.macroImpl[T]
}

@macrocompat.bundle
class DatabaseHelperMacro(override val c: blackbox.Context) extends MacroUtils(c) {
  import c.universe._

  val keySpaceTpe = tq"com.outworkers.phantom.connectors.KeySpace"

  def macroImpl[T <: Database[T] : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]
    val tableSymbol = tq"com.outworkers.phantom.CassandraTable[_, _]"

    val accessors = filterMembers[T, CassandraTable[_, _]]()

    val prefix = q"com.outworkers.phantom.database"

    val tableList = accessors.map(sym => {
      val name = sym.asTerm.name.toTermName
      q"""db.$name"""
    })

    val queryList = tableList.map { tb => q"""$tb.autocreate(space)""" }

    val listType = tq"$prefix.ExecutableCreateStatementsList"

    q"""
       new com.outworkers.phantom.macros.DatabaseHelper[$tpe] {
         def tables(db: $tpe): scala.collection.immutable.Set[$tableSymbol] = {
           scala.collection.immutable.Set.apply[$tableSymbol](..$tableList)
         }

         def createQueries(db: $tpe)(implicit space: $keySpaceTpe): $listType = {
            new $prefix.ExecutableCreateStatementsList(
              space => scala.collection.immutable.Seq.apply(..$queryList)
            )
         }
       }
     """
  }
}
