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

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.execution.QueryCollection
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.macros.toolbelt.WhiteboxToolbelt

import scala.reflect.macros.whitebox
import scala.collection.Seq

trait DatabaseHelper[T <: Database[T]] {
  def tables(db: T): scala.collection.Seq[CassandraTable[_ ,_]]

  def createQueries(db: T)(implicit keySpace: KeySpace): QueryCollection[Seq]
}

object DatabaseHelper {
  implicit def macroMaterialise[
    T <: Database[T]
  ]: DatabaseHelper[T] = macro DatabaseHelperMacro.materialize[T]
}

class DatabaseHelperMacro(override val c: whitebox.Context) extends WhiteboxToolbelt with RootMacro {
  import c.universe._

  private[this] val seqTpe: Type => Tree = { tpe =>
    tq"_root_.scala.collection.Seq[$tpe]"
  }

  private[this] val tableSymbol = typeOf[com.outworkers.phantom.CassandraTable[_, _]]

  private[this] val seqCmp = q"_root_.scala.collection.Seq"

  def materialize[T <: Database[T] : WeakTypeTag]: Tree = {
    memoize[Type, Tree](WhiteboxToolbelt.ddHelperCache)(weakTypeOf[T], deriveHelper)
  }

  def deriveHelper(tpe: Type): Tree = {
    val accessors = filterMembers[CassandraTable[_, _]](tpe, Some(_))

    val execution = q"_root_.com.outworkers.phantom.builder.query.execution"

    val tableList = accessors.map { sym =>
      val name = sym.asTerm.name.toTermName
      q"db.$name"
    }

    val queryList = tableList.map(tb => q"$tb.autocreate(space).executableQuery")

    val creationTree = if (tableList.isEmpty) {
      q"new $execution.QueryCollection($seqCmp.empty[$execution.ExecutableCqlQuery])"
    } else {
      q"new $execution.QueryCollection($seqCmp.apply(..$queryList))"
    }

    val tree = q"""
       new $macroPkg.DatabaseHelper[$tpe] {
         def tables(db: $tpe): ${seqTpe(tableSymbol)} = {
           $seqCmp.apply[$tableSymbol](..$tableList)
         }

         def createQueries(db: $tpe)(
           implicit space: $keyspaceType
         ): $execution.QueryCollection[_root_.scala.collection.Seq] = {
            $creationTree
         }
       }
     """

    evalTree {
      echo(s"Generating type tree for ${showCode(q"$macroPkg.DatabaseHelper[$tpe]")}")
      tree
    }
  }
}
