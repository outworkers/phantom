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

import com.outworkers.phantom.SelectTable
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.dsl.CassandraTable

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  //def fromRow(row: Row): R

  def fields(table: CassandraTable[T, R]): Set[AbstractColumn[_]]
}

object TableHelper {
  implicit def fieldsMacro[T <: CassandraTable[T, R], R]: TableHelper[T, R] = macro TableHelperMacro.macroImpl[T, R]
}

@macrocompat.bundle
class TableHelperMacro(override val c: blackbox.Context) extends MacroUtils(c) {

  import c.universe._

  val knownList = List("Any", "Object", "RootConnector")

  val tableSym = typeOf[CassandraTable[_, _]].typeSymbol
  val selectTable = typeOf[SelectTable[_, _]].typeSymbol
  val rootConn = typeOf[SelectTable[_, _]].typeSymbol

  val exclusions: Symbol => Option[Symbol] = s => {
    val sig = s.typeSignature.typeSymbol

    if (sig == tableSym || sig == selectTable || sig == rootConn) {
      None
    } else {
      Some(s)
    }
  }

  def findRealTable(tpe: Type, target: Symbol): Option[Symbol] = {
    tpe.baseClasses.find(clz => {
      val base = baseType(clz)

      if (base.isEmpty) {
        c.abort(
          c.enclosingPosition,
          s"Could not find a direct ancestor for type ${tpe.typeSymbol.name.decodedName.toString}"
        )
      }

      base.head == target
    })
  }


  def macroImpl[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    val sourceName = tpe.typeSymbol.name.decodedName
    val rTpe = weakTypeOf[R]

    val colTpe = tq"com.outworkers.phantom.column.AbstractColumn[_]"
    val tableTpe = tq"com.outworkers.phantom.CassandraTable[$tpe, $rTpe]"

    val realTable = findRealTable(tpe, tableSym)

    val realType = realTable match {
      case Some(tb) => tb.asType
      case None => c.abort(c.enclosingPosition, s"Could not find out the name of ${sourceName.toString}")
    }

    val finalName = {
      val initial = realType.asType.name.decodedName.toString
      // Lower case the first letter of the type
      // This will make sure the macro derived name is compatible
      // with the old school namin structure used in phantom.
      initial(0).toLower + initial.drop(1)
    }

    val accessors = filterMembers[T, AbstractColumn[_]](exclusions)
      .map(_.asTerm.name).map(tm => q"table.asInstanceOf[$realType].${tm.toTermName}")

    val rowType = tq"com.datastax.driver.core.Row"
    val strTpe = tq"java.lang.String"

    q"""
       new com.outworkers.phantom.macros.TableHelper[$tpe, $rTpe] {
          def tableName: $strTpe = $finalName

          def fields(table: $tableTpe): scala.collection.immutable.Set[$colTpe] = {
            scala.collection.immutable.Set.apply[$colTpe](..$accessors)
          }
       }
     """
  }

}