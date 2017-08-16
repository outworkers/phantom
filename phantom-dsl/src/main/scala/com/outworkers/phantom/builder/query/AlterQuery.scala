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

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.ops.DropColumn
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.execution.ExecutableCqlQuery
import com.outworkers.phantom.builder.query.options.{TablePropertyClause, WithBound, WithChainned, WithUnchainned}
import com.outworkers.phantom.builder.{ConsistencyBound, QueryBuilder, Unspecified}
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace

class AlterQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  Chain <: WithBound
](table: Table, val qb: CQLQuery, val options: QueryOptions) extends RootQuery[Table, Record, Status] {

  final def add(column: String, columnType: String, static: Boolean = false): AlterQuery[Table, Record, Status, Chain] = {
    val query = if (static) {
      QueryBuilder.Alter.addStatic(qb, column, columnType)
    } else {
      QueryBuilder.Alter.add(qb, column, columnType)
    }

    new AlterQuery(table, query, options)
  }

  final def add(definition: CQLQuery): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.add(qb, definition), options)
  }

  final def alter[RR](columnSelect: Table => AbstractColumn[RR], newType: String): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.alter(qb, columnSelect(table).name, newType), options)
  }

  final def rename[RR](select: Table => AbstractColumn[RR], newName: String) : AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.rename(qb, select(table).name, newName), options)
  }

  /**
   * Creates an ALTER drop query to drop the column from the schema definition.
   * It will produce the following type of queries, with the CQL serialization on the right hand side:
   *
   * {{{
   *   MyTable.alter.drop(_.mycolumn) => ALTER TABLE MyTable DROP myColumn
   * }}}
   *
   * @param columnSelect A column selector higher order function derived from a table.
   * @tparam RR The underlying type of the AbstractColumn.
   * @return A new alter query with the underlying builder containing a DROP clause.
   */
  final def drop[RR](columnSelect: Table => DropColumn[RR]): AlterQuery[Table, Record, Status, Chain] = {
    drop(columnSelect(table).column.name)
  }

  /**
   * Creates an ALTER DROP query that drops an entire table.
   * This is equivalent to table truncation followed by table removal from the keyspace metadata.
   * This action is irreversible and you should exercise caution is using it.
   *
   * @param keySpace The implicit keyspace definition to use.
   * @return An alter query with a DROP TABLE instruction encoded in the query string.
   */
  final def drop()(implicit keySpace: KeySpace): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.dropTable(table.tableName, keySpace.name), options)
  }

  final def dropIfExists()(implicit keySpace: KeySpace): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.dropTableIfExist(table.tableName, keySpace.name), options)
  }

  /**
   * Creates an ALTER drop query to drop the column from the schema definition.
   * It will produce the following type of queries, with the CQL serialization on the right hand side:
   *
   * {{{
   *   MyTable.alter.drop(_.mycolumn) => ALTER TABLE MyTable DROP myColumn
   * }}}
   *
   * This is used mainly during the autodiffing of schemas, where column selectors are not available
   * and we only deal with plain string diffs between table metadata collections.
   *
   * @param column The string name of the column to drop.
   * @return A new alter query with the underlying builder containing a DROP clause.
   */
  final def drop(column: String): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.drop(qb, column), options)
  }

  @deprecated("Use option instead", "2.0.0")
  final def `with`(clause: TablePropertyClause)(
    implicit ev: Chain =:= WithUnchainned
  ): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Alter.option(qb, clause.qb), options)
  }

  final def option(clause: TablePropertyClause)(
    implicit ev: Chain =:= WithUnchainned
  ): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Alter.option(qb, clause.qb), options)
  }

  final def and(clause: TablePropertyClause)(implicit ev: Chain =:= WithChainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Where.and(qb, clause.qb), options)
  }

  override def executableQuery: ExecutableCqlQuery = ExecutableCqlQuery(qb, options)
}

object AlterQuery {

  /**
   * An alias for the default AlterQuery type, available to public accessors.
   * Allows for simple API usage of type member where a method can pre-set all the phantom types
   * of all alter query in the background, without leaking them to public APIs.
   *
   * {{{
   *   def alter[T <: CassandraTable[T, _], R]: AlterQuery.Default[T, R]
   * }}}
   *
   * @tparam T The type of the underlying Cassandra table.
   * @tparam R The type of the record stored in the Cassandra table.
   */
  type Default[T <: CassandraTable[T, _], R] = AlterQuery[T, R, Unspecified, WithUnchainned]

  /**
   * The root builder of an ALTER query.
   * This will initialise the alter builder chain and provide the initial {{{ ALTER $TABLENAME }}} query.
   * @param table The table to alter.
   * @tparam T The type of the table.
   * @tparam R The record held in the table.
   * @return A raw ALTER query, without any further options set on it.
   */
  def apply[T <: CassandraTable[T, _], R](table: T)(
    implicit keySpace: KeySpace
  ): AlterQuery.Default[T, R] = {
    new AlterQuery[T, R, Unspecified, WithUnchainned](
      table,
      QueryBuilder.Alter.alter(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString),
      QueryOptions.empty
    )
  }

  def alterType[
    T <: CassandraTable[T, _],
    R,
    NewType
  ](table: T, select: T => AbstractColumn[R], newType: Primitive[NewType])(
    implicit keySpace: KeySpace
  ): AlterQuery.Default[T, R] = {

    val qb = QueryBuilder.Alter.alter(
      QueryBuilder.keyspace(keySpace.name, table.tableName).queryString
    )

    new AlterQuery(
      table,
      QueryBuilder.Alter.alter(qb, select(table).name, newType.dataType),
      QueryOptions.empty
    )
  }

  def alterName[
    T <: CassandraTable[T, _],
    R
  ](table: T, select: T => AbstractColumn[R], newName: String)(
    implicit keySpace: KeySpace
  ): AlterQuery.Default[T, R] = {

    val qb = QueryBuilder.Alter.alter(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString)
    new AlterQuery(
      table,
      QueryBuilder.Alter.rename(qb, select(table).name, newName),
      QueryOptions.empty
    )
  }
}