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

import com.google.common.base.CaseFormat
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.builder.query.sasi.Mode
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.keys.{ClusteringOrder, PartitionKey, PrimaryKey, SASIIndex}
import com.outworkers.phantom.macros.toolbelt.WhiteboxToolbelt
import com.outworkers.phantom.{CassandraTable, NamingStrategy, Row}
import shapeless.HList

import scala.annotation.implicitNotFound
import scala.collection.immutable.ListMap
import scala.reflect.macros.whitebox

@implicitNotFound(msg = """
    | Table ${T} is most likely missing a PartitionKey column.
    | Also check that the fields in your table match types inside ${R}.
  """.stripMargin
)
trait TableHelper[T <: CassandraTable[T, R], R] extends Serializable {

  type Repr <: HList

  def tableName: String

  def fromRow(table: T, row: Row): R

  def tableKey(table: T): String

  def fields(table: T): Seq[AbstractColumn[_]]

  def sasiIndexes(table: T): Seq[SASIIndex[_ <: Mode]]

  def store(table: T, input: Repr)(implicit space: KeySpace): InsertQuery.Default[T, R]
}

object TableHelper {
  implicit def fieldsMacro[
    T <: CassandraTable[T, R],
    R
  ]: TableHelper[T, R] = macro TableHelperMacro.materialize[T, R]

  def apply[T <: CassandraTable[T, R], R](implicit ev: TableHelper[T, R]): TableHelper[T, R] = ev

  type Aux[T <: CassandraTable[T, R], R, Repr0] = TableHelper[T, R] { type Repr = Repr0 }
}

class TableHelperMacro(override val c: whitebox.Context) extends WhiteboxToolbelt with RootMacro {
  import c.universe._

  val exclusions: Symbol => Option[Symbol] = s => {
    val sig = s.typeSignature.typeSymbol

    if (sig == tableSym || sig == selectTable || sig == rootConn) {
      None
    } else {
      Some(s)
    }
  }

  /**
    * A set of reserved CQL keywords that should not be used as column names.
    * They are described here: [[http://docs.datastax.com/en/cql/3.1/cql/cql_reference/keywords_r.html]].
    */
  protected[this] val forbiddenNames = Set(
    TermName("set"),
    TermName("list"),
    TermName("map"),
    TermName("provider")
  )

  protected[this] val columnNameRegex = "^[a-zA-Z0-9_]*$"

  protected[this] def validateColumnName(termName: TermName): TermName = {
    if (
      forbiddenNames.exists(_.toString.toLowerCase == termName.toString.toLowerCase) ||
      !termName.toString.matches(columnNameRegex)
    ) {
      abort(s"Invalid column name $termName, column names cannot be ${forbiddenNames.mkString(", ")} and they have to match $columnNameRegex")
    } else {
      termName
    }
  }

  protected[this] def insertQueryType(table: Type, record: Type): Tree = {
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

  /**
    * Predicate that checks two fields refer to the same type.
    * @param left The source, which is a tuple of two [[Record.Field]] values.
    * @param right The source, which is a tuple of two [[Column.Field]] values.
    * @return True if the left hand side of te tuple is equal to the right hand side.
    *         Not true even if there is an implicit conversion from the left field type to the right field type,
    *         we do not currently support the type mapping natively in the macro.
    */
  private[this] def predicate(left: Record.Field, right: Type): Boolean = {
    left.tpe.dealias =:= right.dealias // || (c.inferImplicitView(EmptyTree, left.tpe, right) != EmptyTree)
  }

  def variations(term: TermName): List[TermName] = {
    val str = lowercased(term).decodedName.toString

    List(
      CaseFormat.LOWER_HYPHEN.to(CaseFormat.LOWER_CAMEL, str),
      CaseFormat.LOWER_UNDERSCORE.to(CaseFormat.LOWER_CAMEL, str),
      CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, str)
    ).distinct.map(TermName(_))
  }

  def lowercased(term: TermName): TermName = {
    TermName(term.decodedName.toString.trim.toLowerCase)
  }

  /**
    * This works by recursively parsing a list of fields extracted here as record members.
    * The algorithm will take every field from the record and:
    * - If there are record fields to address left, we will search within the available columns
    * for a type that either matches or can be implicitly converted to the record type.
    * - If a single match is found, we declare that as a match, without comparing the field names.
    * - If there is more than one match found, we look for a column with a name that matches the record
    * field name.
    * - If a matching name is found, it means we have both a matching type and name and we consider that
    * a correct match.
    * - If no matching name is found, this is appended to the unprocessed list of record fields. We do this because
    * we need to resort to different techniques to deal with unmatched record fields or fields with multiple possible
    * matches. Until 2.6.0, we resorted to using the first column field of the correct type as per user input, in
    * situations where a given record type cou;d match more than one column. However, this introduces a subtle problem
    * as we risk "using up" a column field with a potentially incorrect matching record field because we do not exhaust
    * all "direct" easy matches before attempting to handle the more complex situations.
    * - If a direct match is found or no matching type is found we recursively remove from both the list of record
    * fields to look for and also from the dictionary of column members to look up from.
    * @param columnFields An ordered "as-written" map of column types with a list of terms associated with it. This is used to
    *                      deal with the fact that multiple table columns can have the same Scala type.
    * @param recordFields An ordered "as-written" list of record fields.
    * @param descriptor A table descriptor, built recursively, which will hold all the information we need to generate the
    *                   extractor at the end of this recursive cycle.
    * @param unprocessed The list of unprocessed record fields, dealt with last to avoid the above described scenario. We
    *                    attempt to make all "easy matches" before analysing situations where it's harder to derive
    *                    a simple field match.
    * @return A [[TableDescriptor]], which contains all the information needed to create a full cassandra table.
    */
  def extractorRec(
    columnFields: ListMap[Type, Seq[TermName]],
    recordFields: List[Record.Field],
    descriptor: TableDescriptor,
    unprocessed: List[Record.Field] = Nil
  ): TableDescriptor = {
    recordFields match { case recField :: tail =>

      columnFields.find { case (tpe, _) => predicate(recField, tpe) } map { case (_, seq) => seq } match {
        case None =>
          val un = Unmatched(recField, s"Table doesn't contain a column of type ${printType(recField.tpe)}")
          extractorRec(columnFields, tail, descriptor withoutMatch un, unprocessed)

        case Some(Seq(h)) =>
          extractorRec(
            columnFields - recField.tpe,
            tail,
            descriptor withMatch MatchedField(recField, Column.Field(h, recField.tpe)),
            unprocessed
          )

        case Some(seq) => seq.find(recField.name ==) match {
          case Some(matchingName) =>
            info(s"Found multiple possible matches for ${recField.debugString}")
            val m = MatchedField(recField, Column.Field(matchingName, recField.tpe))

            extractorRec(
              columnFields remove(recField.tpe, matchingName),
              tail,
              descriptor withMatch m,
              unprocessed
            )

          case None =>
            // we now attempt to match a few variations of the term name.
            // and check if the column members contain some possible variations.
            val possibilities = variations(recField.name)

            seq.find(colTerm => possibilities.exists(lowercased(colTerm) ==)) match {
              case Some(matchingName) =>
                val m = MatchedField(recField, Column.Field(matchingName, recField.tpe))
                extractorRec(columnFields remove(recField.tpe, matchingName), tail, descriptor withMatch m)

              case None =>
                // This is still our worst case scenario, where no variation of a term name
                // was found and we still have multiple potential matches for a record field.
                // Under such circumstances we use the first available column term name
                // with respect to the write order.
                val firstName = seq.headOption.getOrElse(
                  abort("Found empty term sequence which should never happen!!!")
                )

                val m = MatchedField(recField, Column.Field(firstName, recField.tpe))
                extractorRec(columnFields remove(recField.tpe, firstName), tail, descriptor withMatch m)
            }
        }
      }

      // return a descriptor where the sequence of unmatched table columns
      // is the original list minus all the elements missing
      case Nil => descriptor
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
    * The fields do not have to be in the same order in both the record and the table. The macro
    * algorithm will go to some length to try and figure out a correct match even if the fields are in random order.
    *
    * {{{
    *   case class MyRecord(
    *     id: UUID,
    *     email: String,
    *     date: DateTime
    *   )
    *
    *   class MyTable extends Table[MyTable, MyRecord] {
    *     object id extends UUIDColumn with PartitionKey
    *     object email extends StringColumn
    *     object date extends DateTimeColumn
    *   }
    * }}}
    *
    * For example, the below will be a perfect match as well:
    *
    * {{{
    *   case class MyRecord(
    *     date: DateTime
    *     id: UUID,
    *     email: String,
    *   )
    *
    *   class MyTable extends Table[MyTable, MyRecord] {
    *     object id extends UUIDColumn with PartitionKey
    *     object email extends StringColumn
    *     object date extends DateTimeColumn
    *   }
    * }}}
    *
    * For a more detailed description on how this method works, see [[extractorRec]].
    *
    * @return An interpolated tree that will contain the automatically generated implementation
    *         of the fromRow method in a Cassandra Table.
    *         Alternatively, this will return an unimplemented ??? method, provided a correct
    *         definition could not be inferred.
    */
  def extractor[T](tableTpe: Type, recordTpe: Type, columns: List[Symbol]): TableDescriptor = {
    val recordMembers = extractRecordMembers(recordTpe)
    val colFields = extractColumnMembers(tableTpe, columns)

    if (recordMembers.isEmpty) {
      warning(s"Supplied record type $recordTpe has no fields defined, are you sure this is what you want?")
      TableDescriptor.empty(tableTpe, recordTpe, colFields)
    } else {
      extractorRec(
        colFields.typeMap,
        recordMembers.toList,
        TableDescriptor(tableTpe, recordTpe, colFields)
      )
    }
  }

  /**
    * Finds the first type in the type hierarchy for which columns exist as direct members.
    * @param tpe The type of the table.
    * @return An optional symbol, if such a type was found in the type hierarchy.
    */
  def determineReferenceTable(tpe: Type): Option[Symbol] = {
    tpe.baseClasses.reverse.find(symbol =>
      symbol.typeSignature.decls.exists(_.typeSignature <:< typeOf[AbstractColumn[_]])
    )
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

  def materialize[T : WeakTypeTag, R : WeakTypeTag]: Tree = {
    val tt = weakTypeOf[T]
    val rt = weakTypeOf[R]

    memoize[(Type, Type), Tree](WhiteboxToolbelt.tableHelperCache)(tt -> rt, { case (t, r) => macroImpl(t, r)})
  }

  /**
    * This will search the implicit scope for a [[NamingStrategy]] defined.
    * If none is found, this will return the table name as is.
    * @param table The name of the table as derived from the user input.
    * @return A new table name adjusted according to the [[NamingStrategy]].
    */
  def adjustedTableName(table: String): Tree = {
    val strategy = c.inferImplicitValue(typeOf[NamingStrategy], silent = true, withMacrosDisabled = true)

    if (strategy.isEmpty) {
      info("No NamingStrategy found in implicit scope.")
      q"$table"
    } else {
      info(s"Altering table name with strategy ${showCode(strategy)}")
      val tree = q"$strategy.inferName($table)"

      evalTree(tree)
    }
  }

  def macroImpl(tableType: Type, recordType: Type): Tree = {
    val refTable = determineReferenceTable(tableType).map(_.typeSignature).getOrElse(tableType)
    val referenceColumns = refTable.decls.sorted.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])
    val refColumnTypes = referenceColumns.map(_.typeSignature)
    val tableName = extractTableName(refTable)
    val columns = filterMembers[AbstractColumn[_]](tableType, exclusions)

    val descriptor = extractor(tableType, recordType, referenceColumns)
    val abstractFromRow = refTable.member(fromRowName).asMethod
    val fromRowFn = descriptor.fromRow
    val notImplemented = q"???"
    val sasiIndexes = columns.filter(sym => sym.typeSignature <:< sasiIndexTpe) map { index =>
      q"$tableTerm.${index.name.toTermName}"
    }

    if (fromRowFn.isEmpty && abstractFromRow.isAbstract) {
      val unmatched = descriptor.debugList(descriptor.unmatched.map(_.field)).mkString("\n")
      error(
        s"""Please define def fromRow(row: ${showCode(rowType)}): ${printType(recordType)}.
          Found unmatched record columns on ${printType(tableType)}
          $unmatched
        """
      )
    } else {
      info(descriptor.showExtractor)
    }

    val accessors = columns.map(_.asTerm.name).map(tm => q"table.instance.${tm.toTermName}").distinct
    // Validate that column names at compile time.
    // columns.map(col => validateColumnName(col.asTerm.name))

    val clsName = TypeName(c.freshName("anon$"))
    val storeTpe = descriptor.hListStoreType.getOrElse(nothingTpe)
    val storeMethod = descriptor.storeMethod.getOrElse(notImplemented)

    val tree = q"""
       final class $clsName extends $macroPkg.TableHelper[$tableType, $recordType] {
          type Repr = $storeTpe

          def tableName: $strTpe = ${adjustedTableName(tableName)}

          def store($tableTerm: $tableType, $inputTerm: $storeTpe)(
           implicit space: $keyspaceType
          ): $builderPkg.InsertQuery.Default[$tableType, $recordType] = {
            $storeMethod
          }

          def tableKey($tableTerm: $tableType): $strTpe = {
            ${inferPrimaryKey(tableName, tableType, refColumnTypes)}
          }

          def fromRow($tableTerm: $tableType, $rowTerm: $rowType): $recordType = {
            ${descriptor.fromRow.getOrElse(notImplemented)}
          }

          def fields($tableTerm: $tableType): scala.collection.immutable.Seq[$colType] = {
            scala.collection.immutable.Seq.apply[$colType](..$accessors)
          }

          def sasiIndexes($tableTerm: $tableType): scala.collection.immutable.Seq[$sasiIndexTpe] = {
            scala.collection.immutable.Seq.apply[$sasiIndexTpe](..$sasiIndexes)
          }
       }

       new $clsName(): $macroPkg.TableHelper.Aux[$tableType, $recordType, $storeTpe]
    """

    if (showCache) {
      echo(WhiteboxToolbelt.tableHelperCache.show)
    }

    evalTree(tree)
  }
}
