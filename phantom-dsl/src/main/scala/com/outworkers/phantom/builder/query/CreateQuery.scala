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

import com.datastax.driver.core.{ConsistencyLevel, Session}
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder._
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.{ExecutableCqlQuery, QueryCollection}
import com.outworkers.phantom.builder.query.options.{CachingStrategies, TablePropertyClause}
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.connectors.{KeySpace, SessionAugmenterImplicits}

import scala.annotation.implicitNotFound

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
      table.columns.map(_.qb)
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

case class CreateQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound
](
  table: Table,
  init: CQLQuery,
  withClause: WithPart = WithPart.empty,
  usingPart: UsingPart = UsingPart.empty,
  options: QueryOptions = QueryOptions.empty
)(implicit val keySpace: KeySpace) extends SessionAugmenterImplicits {

  def consistencyLevel_=(level: ConsistencyLevel)(implicit session: Session): CreateQuery[Table, Record, Specified] = {
    if (session.protocolConsistency) {
      copy(options = options.consistencyLevel_=(level))
    } else {
      copy(usingPart = usingPart append QueryBuilder.consistencyLevel(level.toString))
    }
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def `with`(clause: TablePropertyClause): CreateQuery[Table, Record, Status] = {
    if (withClause.queries.isEmpty) {
      copy(withClause = withClause append QueryBuilder.Create.`with`(clause.qb))
    } else {
      copy(withClause = withClause append QueryBuilder.Update.and(clause.qb))
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
    val clusteringPairs = table.clusteringColumns.map { col =>
        val order = if (col.isAscending) CQLSyntax.Ordering.asc else CQLSyntax.Ordering.desc
        (col.name, order)
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

  val qb: CQLQuery = (withClause merge WithPart.empty merge usingPart) build init

  def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options)

  def queryString: String = qb.queryString

  private[phantom] val indexList: QueryCollection[Seq] = {
    val name = keySpace.name

    new QueryCollection(table.secondaryKeys map { key =>
      if (key.isMapKeyIndex) {
        ExecutableCqlQuery(QueryBuilder.Create.mapIndex(table.tableName, name, key.name))
      } else if (key.isMapEntryIndex) {
        ExecutableCqlQuery(QueryBuilder.Create.mapEntries(table.tableName, name, key.name))
      } else {
        ExecutableCqlQuery(QueryBuilder.Create.index(table.tableName, name, key.name))
      }
    })
  }

  val queries = new QueryCollection[Seq](Seq(ExecutableCqlQuery(qb, options))) ++ indexList ++ table.sasiQueries
}

object CreateQuery {
  type Default[T <: CassandraTable[T, _], R] = CreateQuery[T, R, Unspecified]
}

private[phantom] trait CreateImplicits extends TablePropertyClauses {

  val Cache: CachingStrategies = Caching

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
