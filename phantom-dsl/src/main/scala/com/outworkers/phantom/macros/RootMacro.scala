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

import com.outworkers.phantom.{CassandraTable, SelectTable}
import com.outworkers.phantom.column.AbstractColumn
import org.slf4j.LoggerFactory

import scala.reflect.macros.blackbox

@macrocompat.bundle
class RootMacro(val c: blackbox.Context) {
  import c.universe._

  protected[this] val logger = LoggerFactory.getLogger(this.getClass)

  protected[this] val rowType = tq"com.datastax.driver.core.Row"
  protected[this] val builder = q"com.outworkers.phantom.builder.QueryBuilder"
  protected[this] val macroPkg = q"com.outworkers.phantom.macros"
  protected[this] val builderPkg = q"com.outworkers.phantom.builder.query"
  protected[this] val strTpe = tq"java.lang.String"
  protected[this] val colType = tq"com.outworkers.phantom.column.AbstractColumn[_]"
  protected[this] val collections = q"scala.collection.immutable"
  protected[this] val rowTerm = TermName("row")
  protected[this] val tableTerm = TermName("table")
  protected[this] val keyspaceType = tq"com.outworkers.phantom.connectors.KeySpace"

  val knownList = List("Any", "Object", "RootConnector")

  val tableSym: Symbol = typeOf[CassandraTable[_, _]].typeSymbol
  val selectTable: Symbol = typeOf[SelectTable[_, _]].typeSymbol
  val rootConn: Symbol = typeOf[SelectTable[_, _]].typeSymbol
  val colSymbol: Symbol = typeOf[AbstractColumn[_]].typeSymbol


   def showCollection[M[X] <: TraversableOnce[X]](traversable: M[Type], sep: String = ", "): String = {
    traversable map(tpe => showCode(tq"$tpe")) mkString sep
  }

  def caseFields(tpe: Type): Iterable[(Name, Type)] = {
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

  /**
    * A "generic" type extractor that's meant to produce a list of fields from a record type.
    * We support a narrow domain of types for automated generation, currently including:
    * - Case classes
    * - Tuples
    *
    * To achieve this, we simply have specific ways of extracting the types from the underlying records,
    * and producing a [[Field]] for each of the members in the product type,
    * @param tpe The underlying record type that was passed as the second argument to a Cassandra table.
    * @return An iterable of fields, each containing a [[TermName]] and a [[Type]] that describe a record member.
    */
  def extractRecordMembers(tpe: Type): Iterable[Field] = {
    tpe.typeSymbol match {
      case sym if sym.name.toTypeName.decodedName.toString.contains("Tuple") => {

        val names = List.tabulate(tpe.typeArgs.size)(identity) map {
          index => TermName("_" + index)
        }

        names.zip(tpe.typeArgs) map Field.apply
      }

      case sym if sym.isClass && sym.asClass.isCaseClass => caseFields(tpe) map Field.tupled

      case _ => Iterable.empty[Field]
    }
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

  case class Field(
    name: TermName,
    tpe: Type
  ) {
    def symbol: Symbol = tpe.typeSymbol
  }

  object Field {
    def apply(tp: (TermName, Type)): Field = {
      val (name, tpe) = tp
      Field(name, tpe)
    }

    def tupled(tp: (Name, Type)): Field = {
      val (name, tpe) = tp
      Field(name.toTermName, tpe)
    }
  }

  def extractColumnMembers(table: Type, columns: List[Symbol]): List[Field] = {
    /**
      * We filter for the members of the table type that
      * directly subclass [[AbstractColumn[_]]. For every one of those methods, we
      * are going to look at what type argument was passed by the specific column definition
      * when extending [[AbstractColumn[_]] as this will tell us the Scala output type
      * of the given column.
      * We create a list of these types and if they match the case class types expected,
      * it means we can auto-generate a fromRow implementation.
      */
    columns.map { member =>
      val memberType = member.typeSignatureIn(table)

      memberType.baseClasses.find(colSymbol ==) match {
        case Some(root) =>
          // Here we expect to have a single type argument or type param
          // because we know root here will point to an AbstractColumn[_] symbol.
          root.typeSignature.typeParams match {
            // We use the special API to see what type was passed through to AbstractColumn[_]
            // with special thanks to https://github.com/joroKr21 for helping me not rip
            // the remainder of my hair off while uncovering this marvelous macro API method.
            case head :: Nil => Field(
              member.asModule.name.toTermName,
              head.asType.toType.asSeenFrom(memberType, colSymbol)
            )
            case _ => c.abort(c.enclosingPosition, "Expected exactly one type parameter provided for root column type")
          }
        case None => c.abort(c.enclosingPosition, s"Could not find root column type for ${member.asModule.name}")
      }
    }
  }


}
