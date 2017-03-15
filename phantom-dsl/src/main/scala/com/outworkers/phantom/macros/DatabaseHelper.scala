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
class DatabaseHelperMacro(override val c: blackbox.Context) extends RootMacro(c) {
  import c.universe._

  private[this] val keySpaceTpe = tq"com.outworkers.phantom.connectors.KeySpace"

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
