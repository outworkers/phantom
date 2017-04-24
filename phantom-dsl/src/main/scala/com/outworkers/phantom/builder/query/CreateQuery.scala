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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{
  ConsistencyLevel,
  Session,
  SimpleStatement
}
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.options.TablePropertyClause
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.{CassandraTable, Manager, ResultSet}

import scala.annotation.implicitNotFound
import scala.concurrent.{ExecutionContextExecutor, Future => ScalaFuture}

class RootCreateQuery[
  Table <: CassandraTable[Table, _],
  Record
](val table: Table) {

  private[phantom] def default()(implicit keySpace: KeySpace): CQLQuery = {
    QueryBuilder.Create.defaultCreateQuery(
      keySpace.name,
      table.tableName,
      table.tableKey,
      table.columns.map(_.qb)
    )
  }

  private[this] def lightweight()(implicit keySpace: KeySpace): CQLQuery = {
    QueryBuilder.Create.createIfNotExists(
      keySpace.name,
      table.tableName,
      table.tableKey,
      table.columns.map(_.qb).toSeq
    )
  }

  /**
   * Creates a lightweight transaction Create query, only executed if a table with the given name is not found.
   * If the target keyspace already has a table with the given name, nothing will happen.
   * Cassandra will not attempt to merge, overrwrite or do anything, and the operation will be deemed
   * successful, but have no side effects.
   *
   * @param keySpace The name of the keySpace to target.
   * @return A create query executed with a lightweight transactions.
   */
  def ifNotExists()(implicit keySpace: KeySpace): CreateQuery.Default[Table, Record] = {
    if (table.clusteringColumns.nonEmpty) {
      new CreateQuery(
        table,
        lightweight(),
        WithPart.empty
      ).withClustering()
    } else {
      new CreateQuery(
        table,
        lightweight(),
        WithPart.empty
      )
    }
  }
}

class CreateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](
  val table: Table,
  val init: CQLQuery,
  val withClause: WithPart = WithPart.empty,
  val usingPart: UsingPart = UsingPart.empty,
  override val options: QueryOptions = QueryOptions.empty
)(implicit keySpace: KeySpace) extends ExecutableStatement {

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): CreateQuery[Table, Record, Specified] = {
    if (session.protocolConsistency) {
      new CreateQuery(
        table,
        qb,
        withClause,
        usingPart,
        options.consistencyLevel_=(level)
      )
    } else {
      new CreateQuery(
        table,
        init,
        withClause,
        usingPart append QueryBuilder.consistencyLevel(level.toString),
        options
      )
    }
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def `with`(clause: TablePropertyClause): CreateQuery[Table, Record, Status] = {
    if (withClause.list.isEmpty) {
      new CreateQuery(
        table,
        init,
        withClause append QueryBuilder.Create.`with`(clause.qb),
        usingPart,
        options
      )
    } else {
      new CreateQuery(
        table,
        init,
        withClause append QueryBuilder.Update.and(clause.qb),
        usingPart,
        options
      )
    }
  }

  /**
    * Used to automatically define a CLUSTERING ORDER BY clause using the columns already defined in the table.
    * This will use the built in reflection mechanism to fetch all columns defined inside a table.
    * It will then filter the columns that mix in a definition of a clustering key.
    *
    * @return A new Create query, where the builder contains a full clustering clause specified.
    */
  final def withClustering(): CreateQuery[Table, Record, Status] = {
    val clusteringPairs = table.clusteringColumns.map {
      col => {
        val order = if (col.isAscending) CQLSyntax.Ordering.asc else CQLSyntax.Ordering.desc
        (col.name, order)
      }
    }.toList

    `with`(new TablePropertyClause {
      override def qb: CQLQuery = QueryBuilder.Create.clusteringOrder(clusteringPairs)
    })
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def option(clause: TablePropertyClause): CreateQuery[Table, Record, Status] = {
    `with`(clause)
  }

  @implicitNotFound("You have to use `with` before using `and` in a create query.")
  final def and(clause: TablePropertyClause): CreateQuery[Table, Record, Status] = {
    `with`(clause)
  }

  override def qb: CQLQuery = (withClause merge WithPart.empty merge usingPart) build init

  private[phantom] def indexList(name: String): ExecutableStatementList[Seq] = {
    new ExecutableStatementList(table.secondaryKeys map { key =>
      if (key.isMapKeyIndex) {
        QueryBuilder.Create.mapIndex(table.tableName, name, key.name)
      } else if (key.isMapEntryIndex) {
        QueryBuilder.Create.mapEntries(table.tableName, name, key.name)
      } else {
        QueryBuilder.Create.index(table.tableName, name, key.name)
      }
    })
  }

  override def future()(
    implicit session: Session,
    ec: ExecutionContextExecutor
  ): ScalaFuture[ResultSet] = {
    if (table.secondaryKeys.isEmpty) {
      scalaQueryStringExecuteToFuture(new SimpleStatement(qb.terminate.queryString))
    } else {
      super.future() flatMap { res =>
        indexList(keySpace.name).future() map { _ =>
          Manager.logger.debug(s"Creating secondary indexes on ${QueryBuilder.keyspace(keySpace.name, table.tableName).queryString}")
          res
        }
      }
    }
  }
}

object CreateQuery {
  type Default[T <: CassandraTable[T, _], R] = CreateQuery[T, R, Unspecified]
}

private[phantom] trait CreateImplicits extends TablePropertyClauses {

  val Cache = Caching

  def apply[
    T <: CassandraTable[T, _],
    R
  ](root: RootCreateQuery[T, R])(implicit keySpace: KeySpace): CreateQuery.Default[T, R] = {
    if (root.table.clusteringColumns.nonEmpty) {
      new CreateQuery[T, R, Unspecified](
        root.table,
        root.default()
      ).withClustering()
    } else {
      new CreateQuery[T, R, Unspecified](
        root.table,
        root.default()
      )
    }
  }

  implicit def rootCreateQueryToCreateQuery[
    T <: CassandraTable[T, _],
    R
  ](root: RootCreateQuery[T, R])(implicit keySpace: KeySpace): CreateQuery.Default[T, R] = apply(root)
}
