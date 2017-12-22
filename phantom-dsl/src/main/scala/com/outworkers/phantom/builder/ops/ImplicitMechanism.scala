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
package com.outworkers.phantom.builder.ops

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.{CompareAndSetClause, OrderingColumn, WhereClause}
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.builder.query.sasi.{Mode, SASITextOps}
import com.outworkers.phantom.column._
import com.outworkers.phantom.keys._
import shapeless.<:!<

import scala.annotation.implicitNotFound

sealed class DropColumn[RR](val column: AbstractColumn[RR])

sealed class CasConditionalOperators[RR](col: AbstractColumn[RR]) {

  /**
   * DSL method used to chain "is" clauses in Compare-And-Set operations.
   * Using a call to {{is}}, a column is only updated if the conditional clause of the compare-and-set is met.
   *
   * Example:
   *
   * {{{
   *   Recipes.update.where(_.url eqs recipe.url)
   *    .modify(_.description setTo updated)
   *    .onlyIf(_.description is recipe.description)
   *    .future()
   * }}}
   *
   * @param value The value to compare against in the match clause.
   * @return A compare and set clause usable in an "onlyIf" condition.
   */
  final def is(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.eqs(col.name, col.asCql(value)))
  }

  final def isNot(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.notEqs(col.name, col.asCql(value)))
  }

  final def isGt(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.gt(col.name, col.asCql(value)))
  }

  final def isGte(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.gte(col.name, col.asCql(value)))
  }

  final def isLt(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.lt(col.name, col.asCql(value)))
  }

  final def isLte(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.lte(col.name, col.asCql(value)))
  }
}

sealed class MapEntriesConditionals[K : Primitive, V : Primitive](val col: MapKeyUpdateClause[K, V]) {

  /**
    * Generates a Map CONTAINS ENTRY clause that can be used inside a CQL Where condition.
    * This allows users to lookup records by their full entry inside a map column of a table.
    *
    * Key support is not yet enabled in phantom because index generation has to be done differently.
    * Otherwise, there is no support for simultaneous indexing on both KEYS and VALUES of a MAP column.
    * This limitation will be lifted in the future.
    *
    * @param entry The map entry to look for.
    * @return A Where clause.
    */
  final def eqs(entry: V): WhereClause.Condition = {
    new WhereClause.Condition(
      QueryBuilder.Where.containsEntry(col.column, col.keyName, Primitive[V].asCql(entry))
    )
  }

  /**
    * Generates a Map CONTAINS ENTRY clause that can be used inside a CQL Where condition.
    * This allows users to lookup records by their full entry inside a map column of a table.
    *
    * Key support is not yet enabled in phantom because index generation has to be done differently.
    * Otherwise, there is no support for simultaneous indexing on both KEYS and VALUES of a MAP column.
    * This limitation will be lifted in the future.
    *
    * @param mark The prepare mark ? to later bind.
    * @return A Where clause.
    */
  final def eqs(mark: PrepareMark): WhereClause.ParametricCondition[V] = {
    new WhereClause.ParametricCondition[V](
      QueryBuilder.Where.containsEntry(col.column, col.keyName, mark.qb.queryString)
    )
  }
}

sealed class MapConditionals[T <: CassandraTable[T, R], R, K, V](val col: AbstractMapColumn[T, R, K, V]) {

  /**
   * Generates a Map CONTAINS clause that can be used inside a CQL Where condition.
   * This allows users to lookup records by a VALUE inside a map column of a table.
   *
   * @param elem The element to check for in the contains clause.
   * @return A Where clause.
   */
  final def contains(elem: K): WhereClause.Condition = {
    new WhereClause.Condition(
      QueryBuilder.Where.contains(col.name, col.keyAsCql(elem))
    )
  }

  final def contains(mark: PrepareMark): WhereClause.ParametricCondition[V] = {
    new WhereClause.ParametricCondition[V](
      QueryBuilder.Where.contains(col.name, mark.qb.queryString)
    )
  }
}


private[phantom] trait ImplicitMechanism extends ModifyMechanism {

  // implicit lazy val context: ExecutionContextExecutor = Manager.scalaExecutor

  @implicitNotFound(msg = "Compare-and-set queries can only be applied to non indexed primitive columns.")
  implicit final def columnToCasCompareColumn[RR](col: AbstractColumn[RR])(implicit ev: col.type <:!< Indexed): CasConditionalOperators[RR] = {
    new CasConditionalOperators[RR](col)
  }

  @implicitNotFound(msg = "Index columns and counters cannot be dropped!")
  implicit final def columnToDropColumn[T](col: AbstractColumn[T])(implicit ev: col.type <:!< Undroppable): DropColumn[T] = new DropColumn[T](col)

  implicit def orderingColumn[RR](col: AbstractColumn[RR] with PrimaryKey): OrderingColumn[RR] = new OrderingColumn[RR](col)

  /**
    * Definition used to cast a comparison clause to Map entry lookup based on a secondary index.
    * @param cond The column update clause generated from MapColumn.apply(keyValue)
    * @tparam K The type of the key inside the MapColumn.
    * @tparam V The type of the value held inside the MapColumn.
    * @return A MapEntriesConditionals query that allows secondary index operators on map entries.
    */
  implicit def mapColumnDefinitionToEntriesQueryColumn[
    K : Primitive,
    V : Primitive
  ](cond: MapKeyUpdateClause[K, V]): MapEntriesConditionals[K, V] = {
    new MapEntriesConditionals[K, V](cond)
  }

  /**
    * Definition used to cast an index map column with values indexed to a query-able definition.
    * This will allow users to use "CONTAINS" clauses to search for matches based on map values.
    *
    * @param col The map column to cast to a Map column secondary index query.
    * @tparam T The Cassandra table inner type.
    * @tparam R The record type of the table.
    * @tparam K The type of the key held in the map.
    * @tparam V The type of the value held in the map.
    * @return A MapConditionals class with CONTAINS support.
    */
  implicit def mapColumnToQueryColumn[T <: CassandraTable[T, R], R, K, V](
    col: AbstractMapColumn[T, R, K, V] with Index
  )(implicit ev: col.type <:!< Keys): MapConditionals[T, R, K, V] = {
    new MapConditionals(col)
  }

  implicit def sasiGenericOps[RR : Primitive](
    col: AbstractColumn[RR] with SASIIndex[_ <: Mode]
  ): QueryColumn[RR] = {
    new QueryColumn[RR](col.name)
  }

  implicit def sasiTextOps[M <: Mode](
    col: AbstractColumn[String] with SASIIndex[M]
  )(implicit ev: Primitive[String]): SASITextOps[M] = {
    new SASITextOps[M](col.name)
  }
}
