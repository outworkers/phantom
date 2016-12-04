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

import com.datastax.driver.core.Row
import com.outworkers.phantom.SelectTable
import com.outworkers.phantom.column.{AbstractColumn, PrimitiveColumn}
import com.outworkers.phantom.dsl.CassandraTable

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  def fromRow(row: Row): R

  def fields(table: CassandraTable[T, R]): Set[AbstractColumn[_]]
}

object TableHelper {
  implicit def fieldsMacro[T <: CassandraTable[T, R], R]: TableHelper[T, R] = macro TableHelperMacro.macroImpl[T, R]
}

@macrocompat.bundle
class TableHelperMacro(override val c: blackbox.Context) extends MacroUtils(c) {

  import c.universe._

  val rowType = tq"com.datastax.driver.core.Row"

  val knownList = List("Any", "Object", "RootConnector")

  val tableSym = typeOf[CassandraTable[_, _]].typeSymbol
  val selectTable = typeOf[SelectTable[_, _]].typeSymbol
  val rootConn = typeOf[SelectTable[_, _]].typeSymbol
  val colSymbol = typeOf[AbstractColumn[_]].typeSymbol

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

  def materializeExtractor[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tableTpe = weakTypeOf[T]
    val recordTpe = weakTypeOf[R]
    val sourceMembers = filterMembers[T, AbstractColumn[_]](exclusions)

    Console.println(sourceMembers)


    val columns = sourceMembers.map(_.asModule.info)
    val records = fields(recordTpe) map (_._2) toSet

    val colMembers = sourceMembers.map { member =>
      val memberType = member.typeSignatureIn(tableTpe)

      memberType.baseClasses.find(colSymbol ==) match {
        case Some(root) => {

          println(root.asType.typeSignature.widen.dealias.typeArgs)

          root.typeSignatureIn(memberType).typeArgs.headOption match {
            case Some(colSignature) => colSignature
            case None => c.abort(
              c.enclosingPosition,
              s"Could not find the type argument passed to AbstractColumn for ${member.asModule.name}"
            )
          }
        }
        case None => c.abort(c.enclosingPosition, s"Could not find root column type for ${member.asModule.name}")
      }
    }

    Console.println("The column types in the type tree")
    Console.println(colMembers.map(_.typeSymbol.name.decodedName.toString).mkString("\n"))
    Console.println("The records types in the type tree")
    Console.println(records.map(_.typeSymbol.name.decodedName.toString).mkString("\n"))

    val methodTree = q"""
      null.asInstanceOf[$recordTpe]
    """

    println(showCode(methodTree))
    methodTree
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
      // with the old school naming structure used in phantom.
      initial(0).toLower + initial.drop(1)
    }

    val accessors = filterMembers[T, AbstractColumn[_]](exclusions)
      .map(_.asTerm.name).map(tm => q"table.asInstanceOf[$realType].${tm.toTermName}")

    val rowType = tq"com.datastax.driver.core.Row"
    val strTpe = tq"java.lang.String"

    val tree = q"""
       new com.outworkers.phantom.macros.TableHelper[$tpe, $rTpe] {
          def tableName: $strTpe = $finalName

          def fromRow(row: $rowType): $rTpe = {
            return ${materializeExtractor[T, R]}
          }

          def fields(table: $tableTpe): scala.collection.immutable.Set[$colTpe] = {
            scala.collection.immutable.Set.apply[$colTpe](..$accessors)
          }
       }
     """
    println(showCode(tree))
    tree
  }

}