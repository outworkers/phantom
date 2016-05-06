/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.ops.DropColumn
import com.websudos.phantom.builder.query.options.{TablePropertyClause, WithBound, WithChainned, WithUnchainned}
import com.websudos.phantom.builder.{ConsistencyBound, QueryBuilder, Unspecified}
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound


class AlterQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  Chain <: WithBound
](table: Table, val qb: CQLQuery, override val options: QueryOptions) extends ExecutableStatement {

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

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def `with`(clause: TablePropertyClause)(implicit ev: Chain =:= WithUnchainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Alter.`with`(qb, clause.qb), options)
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def option(clause: TablePropertyClause)(implicit ev: Chain =:= WithUnchainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Alter.`with`(qb, clause.qb), options)
  }

  @implicitNotFound("You have to use `with` before using `and` in a create query.")
  final def and(clause: TablePropertyClause)(implicit ev: Chain =:= WithChainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Where.and(qb, clause.qb), options)
  }

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
  def apply[T <: CassandraTable[T, _], R](table: T)(implicit keySpace: KeySpace): AlterQuery.Default[T, R] = {
    new AlterQuery[T, R, Unspecified, WithUnchainned](
      table,
      QueryBuilder.Alter.alter(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString),
      QueryOptions.empty
    )
  }
}