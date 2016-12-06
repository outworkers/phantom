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

import scala.collection.immutable.ListSet
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

  /**
    * Materializes an extractor method for a table, the so called "fromRow" method.
    *
    * This will only work if the types of the [[R]] record type match the types
    * inferred by the return types of the columns inside the table.
    *
    * If the implementation could not be inferred, the output of this method will be the unimplemented
    * method exception and the user will have to manually override the fromRow definition and create one
    * themselves.
    *
    * {{{
    *   def fromRow(row: Row): R = ???
    * }}}
    *
    * Not only that but they also have to be in the same order. For example:
    * {{{
    *   case class MyRecord(
    *     id: UUID,
    *     email: String,
    *     date: DateTime
    *   )
    *
    *   class MyTable extends CassandraTable[MyTable, MyRecord] {
    *     object id extends UUIDColumn(this) with PartitionKey
    *     object email extends StringColumn(this)
    *     object date extends DateTimeColumn(this)
    *   }
    * }}}
    *
    * @tparam T The type of the Cassandra table to infer the extractor for.
    * @tparam R The record type to infer the extractor for.
    * @return An interpolated tree that will contain the automatically generated implementation
    *         of the fromRow method in a Cassandra Table.
    *         Alternatively, this will return an unimplemented ??? method, provided a correct
    *         definition could not be inferred.
    */
  def materializeExtractor[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tableTpe = weakTypeOf[T]
    val recordTpe = weakTypeOf[R]

    val rowTerm = TermName("row")

    val sourceMembers = filterMembers[T, AbstractColumn[_]](exclusions)
    val columnNames = sourceMembers.map(
      tpe => {
        q"${tpe.typeSignatureIn(tableTpe).typeSymbol.name.toTermName}.apply($rowTerm)"
      }
    )

    /**
      * First we create a set of ordered types corresponding to the type signatures
      * found in the case class arguments.
      */
    val recordMembers = fields(recordTpe) map (_._2)

    /**
      * Then we do the same for the columns, we filter for the members of [[T]] that
      * directly subclass [[AbstractColumn[_]]. For every one of those methods, we
      * are going to look at what type argumnt was passed by the specific column definition
      * when extending [[AbstractColumn[_]] as this will tell us the Scala output type
      * of the given column.
      * We create a list of these types and if they match the case class types expected,
      * it means we can auto-generate a fromRow implementation.
      */
    val colMembers = sourceMembers.map { member =>
      val memberType = member.typeSignatureIn(tableTpe)

      memberType.baseClasses.find(colSymbol ==) match {
        case Some(root) =>
          // Here we expect to have a single type argument or type param
          // because we know root here will point to an AbstractColumn[_] symbol.
          root.typeSignature.typeParams match {
            // We use the special API to see what type was passed through to AbstractColumn[_]
            // with special thanks to https://github.com/joroKr21 for helping me not rip
            // the remainder of my hair off while uncovering this marvelous macro API method.
            case head :: Nil => head.asType.toType.asSeenFrom(memberType, colSymbol)
            case _ => c.abort(c.enclosingPosition, "Expected exactly one type parameter provided for root column type")
          }
        case None => c.abort(c.enclosingPosition, s"Could not find root column type for ${member.asModule.name}")
      }
    }

    val recordMembersSet = recordMembers.to[ListSet]

    Console.println(s"The ${recordMembersSet.size} case class types")
    Console.println(recordMembersSet.map(_.typeSymbol.fullName).mkString("\n"))

    Console.println(s"The ${colMembers.size} output types inferred from the schema definition")
    Console.println(colMembers.map(_.typeSymbol.fullName).mkString("\n"))

    val tree = q"""new ${recordTpe}(..$columnNames)"""

    println(showCode(tree))
    tree
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

          def fromRow(row: $rowType): $rTpe = ${materializeExtractor[T, R]}

          def fields(table: $tableTpe): scala.collection.immutable.Set[$colTpe] = {
            scala.collection.immutable.Set.apply[$colTpe](..$accessors)
          }
       }
     """
    println(showCode(tree))
    tree
  }

}