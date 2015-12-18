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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.clauses.{WhereClause, OrderingColumn, CompareAndSetClause}
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.column._
import com.websudos.phantom.dsl._
import com.websudos.phantom.keys.{Undroppable, Indexed}
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

  final def gt(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.gt(col.name, col.asCql(value)))
  }

  final def gte(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.gte(col.name, col.asCql(value)))
  }

  final def lt(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.lt(col.name, col.asCql(value)))
  }

  final def lte(value: RR): CompareAndSetClause.Condition = {
    new CompareAndSetClause.Condition(QueryBuilder.Where.lte(col.name, col.asCql(value)))
  }
}

sealed class SetConditionals[T <: CassandraTable[T, R], R, RR](val col: AbstractSetColumn[T, R, RR]) {

  /**
   * Generates a Set CONTAINS clause that can be used inside a CQL Where condition.
   * @param elem The element to check for in the contains clause.
   * @return A Where clause.
   */
  final def contains(elem: RR): WhereClause.Condition = {
    new WhereClause.Condition(
      QueryBuilder.Where.contains(col.name, col.valueAsCql(elem))
    )
  }
}

sealed class MapKeyConditionals[T <: CassandraTable[T, R], R, K, V](val col: AbstractMapColumn[T, R, K, V]) {
  /**
   * Generates a Map CONTAINS KEY clause that can be used inside a CQL Where condition.
   * This allows users to lookup records by a KEY inside a map column of a table.
   *
   * Key support is not yet enabled in phantom because index generation has to be done differently.
   * Otherwise, there is no support for simultaneous indexing on both KEYS and VALUES of a MAP column.
   * This limitation will be lifted in the future.
   *
   * @param elem The element to check for in the contains clause.
   * @return A Where clause.
   */
  final def containsKey(elem: K): WhereClause.Condition = {
    new WhereClause.Condition(
      QueryBuilder.Where.containsKey(col.name, col.keyAsCql(elem))
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
}


private[phantom] trait ImplicitMechanism extends ModifyMechanism {

  @implicitNotFound(msg = "Compare-and-set queries can only be applied to non indexed primitive columns.")
  implicit final def columnToCasCompareColumn[RR](col: AbstractColumn[RR])(implicit ev: col.type <:!< Indexed): CasConditionalOperators[RR] = {
    new CasConditionalOperators[RR](col)
  }

  @implicitNotFound(msg = "Index columns and counters cannot be dropped!")
  implicit final def columnToDropColumn[T](col: AbstractColumn[T])(implicit ev: col.type <:!< Undroppable): DropColumn[T] = new DropColumn[T](col)

  implicit def indexedToQueryColumn[T : Primitive](col: AbstractColumn[T] with Indexed): QueryColumn[T] = new QueryColumn(col)

  implicit def orderingColumn[RR](col: AbstractColumn[RR] with PrimaryKey[RR]): OrderingColumn[RR] = new OrderingColumn[RR](col)

  implicit def setColumnToQueryColumn[T <: CassandraTable[T, R], R, RR](col: AbstractSetColumn[T, R, RR] with Index[Set[RR]]): SetConditionals[T, R, RR] = {
    new SetConditionals(col)
  }

  implicit def mapColumnToQueryColumn[T <: CassandraTable[T, R], R, K, V](col: AbstractMapColumn[T, R, K, V] with Index[Map[K, V]])(implicit ev: col.type <:!< Keys): MapConditionals[T, R, K, V] = {
    new MapConditionals(col)
  }

  implicit def mapKeysColumnToQueryColumn[T <: CassandraTable[T, R], R, K, V](col: AbstractMapColumn[T, R, K, V] with Index[Map[K, V]] with Keys): MapKeyConditionals[T, R, K, V] = {
    new MapKeyConditionals(col)
  }

}
