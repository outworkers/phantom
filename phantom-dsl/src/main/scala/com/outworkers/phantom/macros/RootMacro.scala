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

import com.outworkers.phantom.builder.query.sasi.Mode
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.keys.SASIIndex
import com.outworkers.phantom.macros.toolbelt.{HListHelpers, WhiteboxToolbelt}
import com.outworkers.phantom.{CassandraTable, SelectTable}

import scala.collection.compat._
import scala.collection.immutable.ListMap
import scala.reflect.macros.whitebox
import scala.Iterable

trait RootMacro extends HListHelpers with WhiteboxToolbelt {
  val c: whitebox.Context
  import c.universe._

  protected[this] val primitivePkg = q"_root_.com.outworkers.phantom.builder.primitives"
  protected[this] val rowType = tq"_root_.com.outworkers.phantom.Row"
  protected[this] val builder = q"_root_.com.outworkers.phantom.builder.QueryBuilder"
  protected[this] val macroPkg = q"_root_.com.outworkers.phantom.macros"
  protected[this] val builderPkg = q"_root_.com.outworkers.phantom.builder.query"
  protected[this] val enginePkg = q"_root_.com.outworkers.phantom.builder.query.engine"
  protected[this] val strTpe = tq"_root_.java.lang.String"
  protected[this] val colType = typeOf[com.outworkers.phantom.column.AbstractColumn[_]]
  protected[this] val sasiIndexTpe = typeOf[SASIIndex[_ <: Mode]]
  protected[this] val collections = q"_root_.scala.collection.immutable"
  protected[this] val rowTerm = TermName("row")
  protected[this] val tableTerm = TermName("table")
  protected[this] val inputTerm = TermName("input")
  protected[this] val keyspaceType = typeOf[KeySpace]
  protected[this] val nothingTpe: Type = typeOf[scala.Nothing]

  val knownList = List("Any", "Object", "RootConnector")

  val tableSym: Symbol = typeOf[CassandraTable[_, _]].typeSymbol
  val selectTable: Symbol = typeOf[SelectTable[_, _]].typeSymbol
  val rootConn: Symbol = typeOf[SelectTable[_, _]].typeSymbol
  val colSymbol: Symbol = typeOf[AbstractColumn[_]].typeSymbol

  val notImplementedName: TermName = TermName("???")
  val notImplemented: Symbol = typeOf[Predef.type].member(notImplementedName)
  val fromRowName: TermName = TermName("fromRow")

  trait RootField {
    def name: TermName

    def tpe: Type

    def debugString: String = s"${q"$name"} : ${printType(tpe)}"
  }

  object Record {
    case class Field(name: TermName, tpe: Type, index: Int) extends RootField
  }

  object Column {
    case class Field(name: TermName, tpe: Type) extends RootField
  }

  def caseFields(tpe: Type): Seq[(Name, Type)] = {
    object CaseField {
      def unapply(arg: TermSymbol): Option[(Name, Type)] = {
        if (arg.isVal && arg.isCaseAccessor) {
          Some(TermName(arg.name.toString.trim) -> arg.typeSignature)
        } else {
          None
        }
      }
    }

    tpe.decls.toSeq.collect { case CaseField(name, fType) => name -> fType }
  }

  implicit class FieldOps(val col: Seq[RootField]) {
    def typeMap: ListMap[Type, Seq[TermName]] = {
      col.foldLeft(ListMap.empty[Type, Seq[TermName]]) { case (acc, f) =>
        acc + (f.tpe -> (acc.getOrElse(f.tpe, Seq.empty[TermName]) :+ f.name))
      }
    }
  }

  def tupleTerm(index: Int, aug: Int = 1): TermName = {
    TermName("_" + (index + aug).toString)
  }

  trait RecordMatch

  case class Unmatched(
    field: Record.Field,
    reason: String = ""
  ) extends RecordMatch

  case class MatchedField(
    left: Record.Field,
    right: Column.Field
  ) extends RecordMatch {
    def column: Column.Field = right

    def record: Record.Field = left
  }

  implicit class ListMapOps[K, V, M[X] <: Iterable[X]](
    val lm: ListMap[K, M[V]]
  )(implicit cbf: Factory[V, M[V]]) {

    /**
      * Every entry in this ordered map is a traversable of type [[M]].
      * That means every key holds a sequence of elements.
      * This function will remove the element [[elem]] from that sequence
      * for the provided key.
      */
    def remove(key: K, elem: V): ListMap[K, M[V]] = {
      lm.get(key) match {
        case Some(col) => lm + (key -> cbf.fromSpecific(col.filterNot(elem ==)))
        case None => lm
      }
    }
  }

  case class TableDescriptor(
    tableTpe: Type,
    recordType: Type,
    members: Seq[Column.Field],
    matches: Seq[RecordMatch] = Nil
  ) {

    def unmatchedColumns: Seq[Column.Field] = {
      members.filterNot(m => matched.exists(r => r.right.name == m.name))
    }

    def withMatch(m: RecordMatch): TableDescriptor = {
      this.copy(matches = matches :+ m)
    }

    /**
      * This is just done for the naming convenience, but the functionality of distinguishing between
      * matched and unmatched is implemented
      * using an ADT and collect, so it doesn't actually matter if we append to the same place.
      *
      * @param m The record match.
      * @return An immutable copy of the table descriptor with one extra unmatched record.
      */
    def withoutMatch(m: RecordMatch): TableDescriptor = withMatch(m)

    def unmatched: Seq[Unmatched] = matches.collect {
      case u: Unmatched => u
    }

    def matched: Seq[MatchedField] = matches.collect {
      case m: MatchedField => m
    }

    def fromRow: Option[Tree] = {
      if (unmatched.isEmpty) {
        val columnNames = matched.sortBy(_.left.index).map { m =>
          q"$tableTerm.${m.right.name}.apply($rowTerm)"
        }

        Some(q"""new $recordType(..$columnNames)""")
      } else {
        None
      }
    }

    def debugList(fields: Seq[RootField]): Seq[String] = fields.map(u =>
      s"${u.name.decodedName}: ${printType(u.tpe)}"
    )

    /**
      * Creates a map to show users how record fields map to columns inside the table.
      * This is done when they want to inspect the generated macro trees and report
      * bugs and as a convenience feature for us at debugging time.
      * @return An interpolated quoted tree that contains a [[Map[String, String]] definition.
      */
    def debugMap: Tree = {
      val tuples = matched.map(m => {
        val recordTerm = m.record.name.decodedName.toString
        val colTerm = m.record.name.decodedName.toString
        val recordType = printType(m.record.tpe)
        val colType = printType(m.column.tpe)

        q"""
           _root_.scala.Tuple2($recordTerm + ":" + $recordType, $colTerm + ":" + $colType)
        """
      })

      q"_root_.scala.collection.immutable.Map.apply[String, String](..$tuples)"
    }

    /**
      * The reference term is a tuple field pointing to the tuple index found on a store type.
      * If the Cassandra table has more columns than the record field, such as when users
      * chose to store a denormalised variant of a record indexed by a new ID, the store
      * input type will become a tuple of that ID and the record type.
      *
      * So in effect: {{{
      *   case class Record(name: String, timestamp: DateTime)
      *
      *   class Records extends CassandraTable[Records, Record] {
      *
      *     object id extends UUIDColumn with PartitionKey
      *     object name extends StringColumn with PrimaryKey
      *     object timestamp extends DateTimeColumn
      *
      *     // Will end up with a store method that has the following type signature.
      *     def store(input: (UUID, Record)): InsertQuery.Default[Records, Record]
      *   }
      * }}}
      *
      * In these scenarios, we need a way to refer to [[input._index]] as part of the generated
      * store method, where the numerical value of the tuple index is equal to the number of
      * unmatched columns(found in the table but not the record) plus one more for the record type
      * itself and another to compensate for tuples being indexed from 1 instead of 0.
      *
      * @return An optional [[TermName]] of the form [[TermName]]
      */
    val referenceTerm: Option[Tree] = {
      if (unmatchedColumns.isEmpty) {
        Some(hlistNatRef(0))
      } else {
        Some(hlistNatRef(unmatchedColumns.size))
      }
    }

    protected[this] def unmatchedValue(field: Column.Field, ref: Tree): Tree = {
      q"$enginePkg.CQLQuery($tableTerm.${field.name}.asCql($ref))"
    }

    protected[this] def valueTerm(field: MatchedField, refTerm: Option[Tree]): Tree = {
      refTerm match {
        case Some(ref) => q"$enginePkg.CQLQuery($tableTerm.${field.right.name}.asCql($ref.${field.left.name}))"
        case None => q"$enginePkg.CQLQuery($tableTerm.${field.right.name}.asCql($inputTerm.${field.left.name}))"
      }
    }

    /**
      * Short cut method to create a full CQL query using the a particular column
      * inside a table. This will create something like the folloing:
      * {{{
      *  com.outworkers.phantom.
      * }}}
      */
    def tableField(fieldName: TermName): Tree = {
      q"$enginePkg.CQLQuery($tableTerm.$fieldName.name)"
    }

    def hlistNatRef(index: Int): Tree = {
      val indexTerm = TermName("_" + index.toString)

      q"$inputTerm.apply(_root_.shapeless.Nat.$indexTerm)"
    }

    def storeMethod: Option[Tree] = storeType flatMap { sTpe =>
      if (unmatched.isEmpty) {
        val unmatchedColumnInserts = unmatchedColumns.zipWithIndex map { case (field, index) =>
          q"${tableField(field.name)} -> ${unmatchedValue(field, hlistNatRef(index))}"
        }

        val insertions = matched map { field =>
          q"${tableField(field.right.name)} -> ${valueTerm(field, referenceTerm)}"
        }

        val finalDefinitions = unmatchedColumnInserts ++ insertions

        info(s"Inferred store input type: ${printType(sTpe)} for ${printType(tableTpe)}")

        val tree = q"""$tableTerm.`insertValues`(..$finalDefinitions)"""
        Some(tree)
      } else {
        None
      }
    }

    def hListStoreType: Option[Type] = {
      if (unmatchedColumns.isEmpty) {
        Some(mkHListType(List(recordType)))
      } else {
        c.warning(
          c.enclosingPosition,
          s"Found unmatched columns for ${printType(tableTpe)}: ${debugList(unmatchedColumns)}"
        )

        val cols = unmatchedColumns.map(_.tpe) :+ recordType

        if (cols.size > maxTupleSize) {
          c.warning(
            c.enclosingPosition,
            s"Created an HList type of ${cols.size} fields, consider reducing the column count for clarity"
          )
        }

        Some(mkHListType(cols.toList))
      }
    }

    private[this] val maxTupleSize = 22

    /**
     * Automatically creates a [[shapeless.HList]] from the types found in a table as described in the documentation.
     */
    def storeType: Option[Type] = {
      if (unmatchedColumns.isEmpty) {
        Some(mkHListType(recordType :: Nil))
      } else {
        info(s"Found unmatched columns for ${printType(tableTpe)}: ${debugList(unmatchedColumns)}")

        val cols = unmatchedColumns.map(_.tpe) :+ recordType

        if (cols.size > maxTupleSize) {
          c.warning(
            c.enclosingPosition,
            s"Table ${printType(tableTpe)} has ${cols.size} fields, consider reducing the number of columns"
          )
        }
        Some(mkHListType(cols.toList))
      }
    }

    def showExtractor: String = matched.map(f =>
      s"rec.${f.left.name} -> table.${f.right.name} | ${printType(f.right.tpe)}"
    ) mkString "\n"
  }

  object TableDescriptor {
    def empty(table: Type, rec: Type, members: Seq[Column.Field]): TableDescriptor = {
      new TableDescriptor(table, rec, members) {
        override def storeMethod: Option[c.universe.Tree] = None
        override def storeType: Option[Type] = None
        override def fromRow: Option[Tree] = None
      }
    }
  }

  /**
    * A "generic" type extractor that's meant to produce a list of fields from a record type.
    * We support a narrow domain of types for automated generation, currently including:
    * - Case classes
    * - Tuples
    *
    * To achieve this, we simply have specific ways of extracting the types from the underlying records,
    * and producing a [[Record.Field]] for each of the members in the product type,
    *
    * @param tpe The underlying record type that was passed as the second argument to a Cassandra table.
    * @return An iterable of fields, each containing a [[TermName]] and a [[Type]] that describe a record member.
    */
  def extractRecordMembers(tpe: Type): Seq[Record.Field] = {
    tpe.typeSymbol match {
      case sym if sym.fullName.startsWith("scala.Tuple") =>
        (Seq.tabulate(tpe.typeArgs.size)(identity) map {
          index => tupleTerm(index)
        } zip tpe.typeArgs).zipWithIndex map { case ((term, tp), index) =>
          Record.Field(term, tp, index)
        }

      case sym if sym.isClass && sym.asClass.isCaseClass =>
        caseFields(tpe).zipWithIndex map { case ((nm, tp), i) =>
          Record.Field(nm.toTermName, tp, i)
        }

      case _ => Seq.empty[Record.Field]
    }
  }

  def filterMembers[Filter : TypeTag](
    tpe: Type,
    exclusions: Symbol => Option[Symbol]
  ): Seq[Symbol] = {
    (
      for {
        baseClass <- tpe.baseClasses.reverse.flatMap(exclusions(_))
        symbol <- baseClass.typeSignature.members.sorted
        if symbol.typeSignature <:< typeOf[Filter]
      } yield symbol
    ).distinct
  }

  def filterMembers[T : WeakTypeTag, Filter : TypeTag](
    exclusions: Symbol => Option[Symbol] = { s: Symbol => Some(s) }
  ): Seq[Symbol] = {
    filterMembers[Filter](weakTypeOf[T], exclusions)
  }

  def filterColumns[Filter : TypeTag](columns: Seq[Type]): Seq[Type] = {
    columns.filter(_.baseClasses.exists(typeOf[Filter].typeSymbol ==))
  }


  def filterColumns(columns: Seq[Type], filter: Type): Seq[Type] = {
    columns.filter(t => t <:< filter)
  }

  def extractColumnMembers(table: Type, columns: List[Symbol]): List[Column.Field] = {
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
            // off the remainder of my already receding hairline.
            case head :: Nil => Column.Field(
              member.asModule.name.toTermName,
              head.asType.toType.asSeenFrom(memberType, colSymbol)
            )
            case _ => c.abort(
              c.enclosingPosition,
              "Expected exactly one type parameter provided for root column type"
            )
          }
        case None => c.abort(
          c.enclosingPosition,
          s"Could not find root column type for ${member.asModule.name}"
        )
      }
    }
  }

}
