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
package com.outworkers.phantom

import com.datastax.driver.core.Session
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.DeleteClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.execution.ExecutableStatementList
import com.outworkers.phantom.builder.query.sasi.{Analyzer, Mode}
import com.outworkers.phantom.builder.query.{RootCreateQuery, _}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.column.{AbstractColumn, CollectionColumn}
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.keys.SASIIndex
import com.outworkers.phantom.macros.{==:==, SingleGeneric, TableHelper}
import org.slf4j.{Logger, LoggerFactory}
import shapeless.{Generic, HList}

import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContextExecutor, Future}

/**
 * Main representation of a Cassandra table.
 * @tparam T Type of this table.
 * @tparam R Type of record.
 */
abstract class CassandraTable[T <: CassandraTable[T, R], R](
  implicit val helper: TableHelper[T, R]
) extends SelectTable[T, R] { self =>

  @deprecated("Use Table instead of CassandraTable, and skip passing in the 'this' argument", "2.9.1")
  class ListColumn[RR](t: CassandraTable[T, R])(
    implicit ev: Primitive[RR],
    ev2: Primitive[List[RR]]
  ) extends CollectionColumn[T, R, List, RR](t, CQLSyntax.Collections.list)

  @deprecated("Use Table instead of CassandraTable, and skip passing in the 'this' argument", "2.9.1")
  class SetColumn[RR](t: CassandraTable[T, R])(
    implicit ev: Primitive[RR],
    ev2: Primitive[Set[RR]]
  ) extends CollectionColumn[T, R, Set, RR](t, CQLSyntax.Collections.set)

  @deprecated("Use Table instead of CassandraTable, and skip passing in the 'this' argument", "2.9.1")
  class MapColumn[KK, VV](t: CassandraTable[T, R])(
    implicit ev: Primitive[KK],
    ev2: Primitive[VV],
    ev3: Primitive[Map[KK, VV]]
  ) extends com.outworkers.phantom.column.MapColumn[T, R, KK, VV](t)

  def columns: Seq[AbstractColumn[_]] = helper.fields(instance)

  def secondaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isSecondaryKey)

  def primaryKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPrimary).filterNot(_.isPartitionKey)

  def partitionKeys: Seq[AbstractColumn[_]] = columns.filter(_.isPartitionKey)

  def clusteringColumns: Seq[AbstractColumn[_]] = columns.filter(_.isClusteringKey)

  def tableKey: String = helper.tableKey(instance)

  def instance: T = self.asInstanceOf[T]

  lazy val logger: Logger = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  def createSchema()(
    implicit session: Session,
    keySpace: KeySpace,
    ec: ExecutionContextExecutor
  ): ResultSet = Await.result(autocreate(keySpace).future(), 10.seconds)

  def tableName: String = helper.tableName

  def fromRow(r: Row): R = helper.fromRow(instance, r)

  /**
   * The new create mechanism introduced in Phantom 1.6.0.
   * This uses the phantom proprietary QueryBuilder instead of the already available one in the underlying Java Driver.
   * @return A root create block, with full support for all CQL Create query options.
   */
  final def create: RootCreateQuery[T, R] = new RootCreateQuery(instance)

  def autocreate(keySpace: KeySpace): CreateQuery.Default[T, R] = create.ifNotExists()(keySpace)

  final def alter()(implicit keySpace: KeySpace): AlterQuery.Default[T, R] = AlterQuery(instance)

  final def alter[
    RR,
    NewType
  ](columnSelect: T => AbstractColumn[RR])(newType: Primitive[NewType])(
    implicit keySpace: KeySpace
  ): AlterQuery.Default[T, RR] = {
    AlterQuery.alterType[T, RR, NewType](instance, columnSelect, newType)
  }

  final def alter[RR](
    columnSelect: T => AbstractColumn[RR],
    newName: String
  )(implicit keySpace: KeySpace): AlterQuery.Default[T, RR] = {
    AlterQuery.alterName[T, RR](instance, columnSelect, newName)
  }

  final def update()(implicit keySpace: KeySpace): UpdateQuery.Default[T, R] = UpdateQuery(instance)

  final def insert()(implicit keySpace: KeySpace): InsertQuery.Default[T, R] = InsertQuery(instance)

  def sasiQueries()(implicit keySpace: KeySpace): ExecutableStatementList[Seq] = {
    val queries = sasiIndexes.map { index =>
      QueryBuilder.Create.createSASIIndex(
        keySpace,
        tableName,
        QueryBuilder.Create.sasiIndexName(tableName, index.name),
        index.name,
        index.analyzer.qb
      )
    }
    new ExecutableStatementList[Seq](queries)
  }

  def sasiIndexes: Seq[SASIIndex[_ <: Mode]] = helper.sasiIndexes(instance)

  /**
    * Automatically generated store method for the record type.
    * @param input The input which will be auto-tupled and compared.
    * @param keySpace The keyspace in which the query will be executed.
    * @tparam V1 The type of the input.
    * @return A default input query.
    */
  def store[V1, Repr <: HList, HL, Out <: HList](input: V1)(
    implicit keySpace: KeySpace,
    thl: TableHelper.Aux[T, R, Repr],
    gen: Generic.Aux[V1, HL],
    sg: SingleGeneric.Aux[V1, Repr, HL, Out],
    ev: Out ==:== Repr
  ): InsertQuery.Default[T, R] = thl.store(instance, (sg to input).asInstanceOf[Repr])

  def storeRecord[V1, Repr <: HList, HL <: HList, Out <: HList](input: V1)(
    implicit keySpace: KeySpace,
    session: Session,
    thl: TableHelper.Aux[T, R, Repr],
    ex: ExecutionContextExecutor,
    gen: Generic.Aux[V1, HL],
    sg: SingleGeneric.Aux[V1, Repr, HL, Out],
    ev: Out ==:== Repr
  ): Future[ResultSet] = store(input).future()

  def storeRecords[M[X] <: TraversableOnce[X], V1, Repr <: HList, HL <: HList, Out <: HList](inputs: M[V1])(
    implicit keySpace: KeySpace,
    session: Session,
    thl: TableHelper.Aux[T, R, Repr],
    ex: ExecutionContextExecutor,
    gen: Generic.Aux[V1, HL],
    sg: SingleGeneric.Aux[V1, Repr, HL, Out],
    ev: Out ==:== Repr,
    cbf: CanBuildFrom[M[V1], ResultSet, M[ResultSet]]
  ): Future[M[ResultSet]] = {
    Future.traverse(inputs)(el => storeRecord(el))
  }

  final def delete()(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = DeleteQuery[T, R](instance)

  final def delete(
    conditions: (T => DeleteClause.Condition)*
  )(implicit keySpace: KeySpace): DeleteQuery.Default[T, R] = {
    DeleteQuery[T, R](instance, conditions.map(_(instance).qb): _*)
  }

  final def truncate()(
    implicit keySpace: KeySpace
  ): TruncateQuery.Default[T, R] = TruncateQuery[T, R](instance)
}
