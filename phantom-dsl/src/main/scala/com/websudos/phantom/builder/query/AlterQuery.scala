package com.websudos.phantom.builder.query

import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.ops.DropColumn
import com.websudos.phantom.builder.{QueryBuilder, Unspecified, ConsistencyBound}
import com.websudos.phantom.column.AbstractColumn
import com.websudos.phantom.connectors.KeySpace

import scala.annotation.implicitNotFound


class AlterQuery[
  Table <: CassandraTable[Table, _],
  Record,
  Status <: ConsistencyBound,
  Chain <: WithBound
](table: Table, val qb: CQLQuery) extends ExecutableStatement {

  final def add(column: String, columnType: String, static: Boolean = false): AlterQuery[Table, Record, Status, Chain] = {
    val query = if (static) {
      QueryBuilder.Alter.addStatic(qb, column, columnType)
    } else {
      QueryBuilder.Alter.add(qb, column, columnType)
    }

    new AlterQuery(table, query)
  }

  final def add(definition: CQLQuery): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.add(qb, definition))
  }

  final def alter[RR](columnSelect: Table => AbstractColumn[RR], newType: String): AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.alter(qb, columnSelect(table).name, newType))
  }

  final def rename[RR](select: Table => AbstractColumn[RR], newName: String) : AlterQuery[Table, Record, Status, Chain] = {
    new AlterQuery(table, QueryBuilder.Alter.rename(qb, select(table).name, newName))
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
    new AlterQuery(table, QueryBuilder.Alter.drop(qb, column))
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def `with`(clause: TablePropertyClause)(implicit ev: Chain =:= WithUnchainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Create.`with`(qb, clause.qb))
  }

  @implicitNotFound("You cannot use 2 `with` clauses on the same create query. Use `and` instead.")
  final def option(clause: TablePropertyClause)(implicit ev: Chain =:= WithUnchainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Create.`with`(qb, clause.qb))
  }

  @implicitNotFound("You have to use `with` before using `and` in a create query.")
  final def and(clause: TablePropertyClause)(implicit ev: Chain =:= WithChainned): AlterQuery[Table, Record, Status, WithChainned] = {
    new AlterQuery(table, QueryBuilder.Where.and(qb, clause.qb))
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
    new AlterQuery[T, R, Unspecified, WithUnchainned](table, QueryBuilder.alter(QueryBuilder.keyspace(keySpace.name, table.tableName).queryString))
  }
}