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
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.dsl.KeySpace
import com.outworkers.phantom.keys.{ClusteringOrder, PartitionKey, PrimaryKey}
import com.outworkers.phantom.{CassandraTable, SelectTable}
import org.slf4j.LoggerFactory

import scala.reflect.macros.blackbox

trait TableHelper[T <: CassandraTable[T, R], R] {

  def tableName: String

  def fromRow(table: T, row: Row): R

  def tableKey(table: T): String

  def fields(table: T): Seq[AbstractColumn[_]]

  def store(table: T)(implicit space: KeySpace): InsertQuery.Default[T, R]
}

object TableHelper {
  implicit def fieldsMacro[T <: CassandraTable[T, R], R]: TableHelper[T, R] = macro TableHelperMacro.macroImpl[T, R]

  def apply[T <: CassandraTable[T, R], R](implicit ev: TableHelper[T, R]): TableHelper[T, R] = ev
}

@macrocompat.bundle
class TableHelperMacro(override val c: blackbox.Context) extends RootMacro(c) {
  import c.universe._

  val exclusions: Symbol => Option[Symbol] = s => {
    val sig = s.typeSignature.typeSymbol

    if (sig == tableSym || sig == selectTable || sig == rootConn) {
      None
    } else {
      Some(s)
    }
  }

  def filterColumns[Filter : TypeTag](columns: Seq[Type]): Seq[Type] = {
    columns.filter(_.baseClasses.exists(typeOf[Filter].typeSymbol == ))
  }

  def insertQueryType(table: Type, record: Type): Tree = {
    tq"com.outworkers.phantom.builder.query.InsertQuery.Default[$table, $record]"
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
  def inferPrimaryKey(tableName: String, table: Type, columns: Seq[Type]): Tree = {
    val partitionKeys = filterColumns[PartitionKey](columns)
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    if (partitionKeys.isEmpty) {
      c.abort(
        c.enclosingPosition,
        s"Table $tableName needs to have at least one partition key"
      )
    }

    val primaries = filterColumns[PrimaryKey](columns)
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    val clusteringKeys = filterColumns[ClusteringOrder](columns)
      .map(_.typeSymbol.typeSignatureIn(table).typeSymbol.name.toTermName)
      .map(name => q"$tableTerm.$name")

    if (clusteringKeys.nonEmpty && (clusteringKeys.size != primaries.size)) {
      c.abort(
        c.enclosingPosition,
        "Using clustering order on one primary key part " +
          "means all primary key parts must explicitly define clustering. " +
          s"Table $tableName still has ${primaries.size} primary keys defined"
      )
    } else {
      q"""
        $builder.Create.primaryKey(
          $collections.List[$colType](..$partitionKeys).map(_.name),
          $collections.List[$colType](..$primaries).map(_.name)
        ).queryString
      """
    }

  }

  trait TableMatchResult

  case class Match(
    results: List[Field],
    partition: Boolean
  ) extends TableMatchResult

  case object NoMatch extends TableMatchResult

  private[this] def predicate(source: (Field, Field)): Boolean = {
    val (col, rec) = source
    (col.tpe =:= rec.tpe) || (c.inferImplicitView(EmptyTree, col.tpe, rec.tpe) != EmptyTree)
  }

  /**
    * Finds a matching subset of columns inside a table definition where the extracted
    * type from a table does not need to include all of the columns inside a table.
    *
    * This addresses [[https://websudos.atlassian.net/browse/PHANTOM-237]].
    *
    * @param recordMembers The type members of the record type.
    * @param members The type members of the table.
    * @return
    */
  def findMatchingSubset(
    tableName: Name,
    members: List[Field],
    recordMembers: Iterable[Field]
  ): TableMatchResult = {
    if (members.isEmpty) {
      NoMatch
    } else {

      if (members.size >= recordMembers.size && members.zip(recordMembers).forall(predicate)) {
        logger.info(s"Successfully derived extractor for $tableName using columns: ${showCollection(members.map(_.tpe))}")
        Match(members, recordMembers.size != members.size)
      } else {
        findMatchingSubset(tableName, members.tail, recordMembers)
      }
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
  def materializeExtractor[T](tableTpe: Type, recordTpe: Type, columns: List[Symbol]): Option[Tree] = {
    /**
      * First we create a set of ordered types corresponding to the type signatures
      * found in the record. These types are extracted differently based on the specific
      * type passed as an argument to the table.
      */
    val recordMembers = extractRecordMembers(recordTpe)

    val colMembers = extractColumnMembers(tableTpe, columns)

    val tableSymbolName = tableTpe.typeSymbol.name

    findMatchingSubset(tableSymbolName, colMembers, recordMembers) match {
      case Match(results, _) if recordTpe.typeSymbol.isClass && recordTpe.typeSymbol.asClass.isCaseClass => {
        val columnNames = results.map { member =>
          q"$tableTerm.${member.name}.apply($rowTerm)"
        }

        Some(q"""new $recordTpe(..$columnNames)""")
      }
      case _ => {
        Console.println(s"Couldn't automatically infer a fromRow method definition for $tableSymbolName")
        None
      }
    }
  }

  /**
    * Finds the first type in the type hierarchy for which columns exist as direct members.
    * @param tpe The type of the table.
    * @return An optional symbol, if such a type was found in the type hierarchy.
    */
  def determineReferenceTable(tpe: Type): Option[Symbol] = {
    tpe.baseClasses.reverse.find(symbol => {
      symbol.typeSignature.decls.exists(_.typeSignature <:< typeOf[AbstractColumn[_]])
    })
  }

  /**
    * Extracts the name of the table that will be generated and used in Cassandra.
    * This can be changed at runtime by the user by overriding [[CassandraTable.tableName]].
    * This mechanism is incompatible with the historical way we used to do this, effectively using
    * the type inferred by the final database object.
    *
    * Instead, at present times, it is the type hierarchy that dictates what a table will be called,
    * and the first member of the type hierarchy of a table type with columns defined will dictate the name.
    *
    * @param source The source table type to extract the name from. We rely on this to be the first in the hierarchy to
    *               contain column definitions, determined by [[determineReferenceTable()]] above.
    * @return
    */
  def extractTableName(source: Type): String =  {
    val value = source.typeSymbol.name.toTermName.decodedName.toString
    value.charAt(0).toLower + value.drop(1)
  }

  def macroImpl[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tableType = weakTypeOf[T]

    val rTpe = weakTypeOf[R]

    val refTable = determineReferenceTable(tableType).map(_.typeSignature).getOrElse(tableType)
    val referenceColumns = refTable.decls.sorted.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])

    val tableName = extractTableName(refTable)

    val columns = filterMembers[T, AbstractColumn[_]](exclusions)

    val fromRowDefinition = materializeExtractor(tableType, rTpe, referenceColumns) getOrElse q"???"

    val accessors = columns.map(_.asTerm.name).map(tm => q"table.instance.${tm.toTermName}").distinct

    val tree = q"""
       new com.outworkers.phantom.macros.TableHelper[$tableType, $rTpe] {
          def tableName: $strTpe = $tableName

          def store($tableTerm: $tableType)(implicit space: $keyspaceType): ${insertQueryType(tableType, rTpe)} = {
            $tableTerm.insert()
          }

          def tableKey($tableTerm: $tableType): $strTpe = {
            ${inferPrimaryKey(tableName, tableType, referenceColumns.map(_.typeSignature))}
          }

          def fromRow($tableTerm: $tableType, $rowTerm: $rowType): $rTpe = $fromRowDefinition

          def fields($tableTerm: $tableType): scala.collection.immutable.Seq[$colType] = {
            scala.collection.immutable.Seq.apply[$colType](..$accessors)
          }
       }
    """
    tree
  }

}