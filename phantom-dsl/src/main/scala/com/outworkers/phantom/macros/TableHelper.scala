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
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.dsl.{CassandraTable, ClusteringOrder, PartitionKey, PrimaryKey}

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  def fromRow(table: T, row: Row): R

  def tableKey(table: T): String

  def fields(table: CassandraTable[T, R]): Set[AbstractColumn[_]]
}

object TableHelper {
  implicit def fieldsMacro[T <: CassandraTable[T, R], R]: TableHelper[T, R] = macro TableHelperMacro.macroImpl[T, R]
}

@macrocompat.bundle
class TableHelperMacro(override val c: blackbox.Context) extends MacroUtils(c) {

  import c.universe._

  val rowType = tq"com.datastax.driver.core.Row"
  val builder = q"com.outworkers.phantom.builder.QueryBuilder"
  val rowTerm = TermName("row")
  val tableTerm = TermName("table")

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

  /**
    * This method will check for common Cassandra anti-patterns during the intialisation of a schema.
    * If the Schema definition violates valid CQL standard, this function will throw an error.
    *
    * A perfect example is using a mixture of Primary keys and Clustering keys in the same schema.
    * While a Clustering key is also a primary key, when defining a clustering key all other keys must become clustering keys and specify their order.
    *
    * We could auto-generate this order but we wouldn't be making false assumptions about the desired ordering.
    */
  def inferPrimaryKey(table: Type, columns: Seq[Type]): Unit = {
    val tableName = table.typeSymbol.name.decodedName

    if (!columns.exists(_ <:< typeOf[PartitionKey])) {
      c.abort(
        c.enclosingPosition,
        s"Table $tableName needs to have at least one partition key"
      )
    }

    val partitionKeys = columns
      .filter(_ <:< typeOf[PartitionKey])
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    val primaries = columns
      .filter(_ <:< typeOf[PrimaryKey])
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    val clusteringKeys = columns.filter(_ <:< typeOf[ClusteringOrder])
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    if (clusteringKeys.nonEmpty && primaries.nonEmpty) {
      c.abort(
        c.enclosingPosition,
        "Using clustering order on one primary key part " +
          "means all primary key parts must explicitly define clustering. " +
          s"Table $tableName still has ${primaries.size} primary keys defined"
      )
    } else {
      q"$builder.Create.primaryKey(..$partitionKeys, ..$primaries, ..$clusteringKeys)"
    }

  }

  /**
    * Materializes an extractor method for a table, the so called "fromRow" method.
    *
    * This will only work if the types of the record type match the types
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
    * @return An interpolated tree that will contain the automatically generated implementation
    *         of the fromRow method in a Cassandra Table.
    *         Alternatively, this will return an unimplemented ??? method, provided a correct
    *         definition could not be inferred.
    */
  def materializeExtractor(tableTpe: Type, recordTpe: Type): Tree = {
    val sourceMembers = tableTpe.decls.sorted.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])
    val columnNames = sourceMembers.map(
      tpe => {
        q"$tableTerm.${tpe.typeSignatureIn(tableTpe).typeSymbol.name.toTermName}.apply($rowTerm)"
      }
    )

    /**
      * First we create a set of ordered types corresponding to the type signatures
      * found in the case class arguments.
      */
    val recordMembers = fields(recordTpe) map (_._2)

    /**
      * Then we do the same for the columns, we filter for the members of the table type that
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

    if (recordMembers.size == colMembers.size) {
      if (!recordMembers.toSeq.zip(colMembers).forall { case (rec, col) => rec != col }) {
        q"""new $recordTpe(..$columnNames)"""
      } else {
        q"???"
      }
    } else {
      q"???"
    }
  }


  def macroImpl[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tpe = weakTypeOf[T]

    val sourceName = tpe.typeSymbol.name.decodedName
    val rTpe = weakTypeOf[R]

    val colTpe = tq"com.outworkers.phantom.column.AbstractColumn[_]"
    val tableTpe = tq"com.outworkers.phantom.CassandraTable[$tpe, $rTpe]"

    val finalName = tpe.typeSymbol.name.toTermName

    val columns = tpe.decls.sorted.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])

    val accessors = columns.map(_.asTerm.name).map(tm => q"table.instance.${tm.toTermName}")

    val rowType = tq"com.datastax.driver.core.Row"
    val strTpe = tq"java.lang.String"

    val tree = q"""
       new com.outworkers.phantom.macros.TableHelper[$tpe, $rTpe] {
          def tableName: $strTpe = $finalName

          def tableKey($tableTerm: $tpe): $strTpe = ${inferPrimaryKey(tpe, columns.map(_.typeSignature))}

          def fromRow($tableTerm: $tpe, $rowTerm: $rowType): $rTpe = ${materializeExtractor(tpe, rTpe)}

          def fields($tableTerm: $tableTpe): scala.collection.immutable.Set[$colTpe] = {
            scala.collection.immutable.Set.apply[$colTpe](..$accessors)
          }
       }
     """
    tree
  }

}