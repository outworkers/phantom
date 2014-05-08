/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Ordering => TableOrdering }
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.newzly.phantom.keys.{ ClusteringOrder, Index, PartitionKey, PrimaryKey }
import com.newzly.phantom.query._
import com.newzly.phantom.batch.BatchableStatement
import com.newzly.phantom.query.QueryCondition


sealed class OrderingColumn[T](col: AbstractColumn[T]) {
  def asc: QueryOrdering = {
    QueryOrdering(QueryBuilder.asc(col.name))
  }

  def desc: QueryOrdering = {
    QueryOrdering(QueryBuilder.desc(col.name))
  }
}

/**
 * A class enforcing columns used in where clauses to be indexed.
 * Using an implicit mechanism, only columns that are indexed can be converted into Indexed columns.
 * This enforces a Cassandra limitation at compile time.
 * It prevents a user from querying and using where operators on a column without any index.
 * @param col The column to cast to an IndexedColumn.
 * @tparam T The type of the value the column holds.
 */
sealed abstract class AbstractQueryColumn[T: CassandraPrimitive](col: AbstractColumn[T]) extends OrderingColumn[T](col) {

  /**
   * The equals operator. Will return a match if the value equals the database value.
   * @param value The value to search for in the database.
   * @return A QueryCondition, wrapping a QueryBuilder clause.
   */
  def eqs(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.eq(col.name, col.toCType(value)))
  }

  def lt(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lt(col.name, col.toCType(value)))
  }

  def lte(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lte(col.name, col.toCType(value)))
  }

  def gt(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gt(col.name, col.toCType(value)))
  }

  def gte(value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gte(col.name, col.toCType(value)))
  }

  def in(values: List[T]): QueryCondition = {
    QueryCondition(QueryBuilder.in(col.name, values.map(col.toCType): _*))
  }
}


private [phantom] abstract class AbstractModifyColumn[RR](name: String) {

  def toCType(v: RR): AnyRef

  def setTo(value: RR): Assignment = QueryBuilder.set(name, toCType(value))
}

sealed abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}

class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}

class QueryColumn[RR : CassandraPrimitive](col: AbstractColumn[RR]) extends AbstractQueryColumn[RR](col)

sealed trait BatchRestrictions {
  implicit def insertQueryIsBatchable[T <: CassandraTable[T, R], R](query: InsertQuery[T, R]): BatchableStatement = new BatchableStatement(query)
  implicit def assignmentsQueryIsBatchable[T <: CassandraTable[T, R], R](query: AssignmentsQuery[T, R]): BatchableStatement = new BatchableStatement(query)
  implicit def assignmentsOptionQueryIsBatchable[T <: CassandraTable[T, R], R](query: AssignmentOptionQuery[T, R]): BatchableStatement = new BatchableStatement(query)
  implicit def deleteQueryIsBatchable[T <: CassandraTable[T, R], R](query: DeleteQuery[T, R]): BatchableStatement = new BatchableStatement(query)
  implicit def deleteWhereQueryIsBatchable[T <: CassandraTable[T, R], R](query: DeleteWhere[T, R]): BatchableStatement = new BatchableStatement(query)
}

sealed trait CollectionOperators {

  implicit class CounterModifyColumn[Owner <: CassandraTable[Owner, Record], Record](col: CounterColumn[Owner, Record]) {
    def increment(value: Long = 1L): Assignment = QueryBuilder.incr(col.name, value)
    def decrement(value: Long = 1L): Assignment = QueryBuilder.decr(col.name, value)
  }

  implicit class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractListColumn[Owner, Record, RR]) extends ModifyColumn[List[RR]](col) {

    def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, col.valueToCType(value))
    def prependAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.prependAll(col.name, col.valuesToCType(values))
    def append(value: RR): Assignment = QueryBuilder.append(col.name, col.valueToCType(value))
    def appendAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.appendAll(col.name, col.valuesToCType(values))
    def discard(value: RR): Assignment = QueryBuilder.discard(col.name, col.valueToCType(value))
    def discardAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.discardAll(col.name, col.valuesToCType(values))
    def setIdx(i: Int, value: RR): Assignment = QueryBuilder.setIdx(col.name, i, col.valueToCType(value))
  }

  implicit class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractSetColumn[Owner, Record, RR]) extends ModifyColumn[Set[RR]](col) {

    def add(value: RR): Assignment = QueryBuilder.add(col.name, col.valueToCType(value))
    def addAll(values: Set[RR]): Assignment = QueryBuilder.addAll(col.name, col.valuesToCType(values))
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, col.valueToCType(value))
    def removeAll(values: Set[RR]): Assignment = QueryBuilder.removeAll(col.name, col.valuesToCType(values))
  }

  implicit class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A, B](col: AbstractMapColumn[Owner, Record, A, B]) extends ModifyColumn[Map[A, B]](col) {

    def put(value: (A, B)): Assignment = QueryBuilder.put(col.name, col.keyToCType(value._1), col.valueToCType(value._2))
    def putAll[L <% Traversable[(A, B)]](values: L): Assignment = QueryBuilder.putAll(col.name, col.valuesToCType(values))
  }
}

sealed trait OrderingOperators {
  implicit def clusteringKeyToOrderingOperator[T : CassandraPrimitive](col: AbstractColumn[T] with ClusteringOrder[T]): OrderingColumn[T] = new OrderingColumn[T](col)

}

sealed trait IndexRestrictions {
  implicit def partitionColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with PartitionKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def primaryColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with PrimaryKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def secondaryColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with Index[T]): QueryColumn[T] = new QueryColumn(col)
}

sealed class ModifiableColumn[T]
sealed trait ModifyImplicits extends LowPriorityImplicits {

  implicit final def columnsAreModifiable[T <: AbstractColumn[_]] = new ModifiableColumn[T]

  implicit final def countersAreNotModifiable[T <: AbstractColumn[RR] with CounterRestriction[RR], RR] = new ModifiableColumn[T]
  implicit final def countersAreNotModifiable2[T <: AbstractColumn[RR] with CounterRestriction[RR], RR] = new ModifiableColumn[T]

  implicit final def primaryKeysAreNotModifiable[T <: AbstractColumn[RR] with PrimaryKey[RR], RR] = new ModifiableColumn[T]
  implicit final def primaryKeysAreNotModifiable2[T <: AbstractColumn[RR] with PrimaryKey[RR], RR] = new ModifiableColumn[T]

  implicit final def clusteringKeysAreNotModifiable[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR] = new ModifiableColumn[T]
  implicit final def clusteringKeysAreNotModifiable2[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR] = new ModifiableColumn[T]

  implicit final def partitionKeysAreNotModifiable[T <: AbstractColumn[RR] with PartitionKey[RR], RR] = new ModifiableColumn[T]
  implicit final def partitionKeysAreNotModifiable2[T <: AbstractColumn[RR] with PartitionKey[RR], RR] = new ModifiableColumn[T]

  implicit final def indexesAreNotModifiable[T <: AbstractColumn[RR] with Index[RR], RR] = new ModifiableColumn[T]
  implicit final def indexesAreNotModifiable2[T <: AbstractColumn[RR] with Index[RR], RR] = new ModifiableColumn[T]

  implicit final def columnToModifyColumn[T](col: AbstractColumn[T]): ModifyColumn[T] = new ModifyColumn[T](col)

  @implicitNotFound(msg = "CounterColumns can only be incremented or decremented.")
  implicit final def nonCounterColumns[T <: CounterRestriction[RR] : ModifiableColumn, RR](obj: AbstractColumn[RR] with CounterRestriction[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of primary key columns cannot be updated as per the Cassandra specification")
  implicit final def notPrimaryKeys[T <: PrimaryKey[RR] : ModifiableColumn, RR](obj: AbstractColumn[RR] with PrimaryKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of partition key columns cannot be updated as per the Cassandra specification")
  implicit final def notPartitionKeys[T <: PartitionKey[RR] : ModifiableColumn, RR](obj: AbstractColumn[RR] with PartitionKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of indexed columns cannot be updated as per the Cassandra specification")
  implicit final def notIndexKeys[T <: PartitionKey[RR] : ModifiableColumn, RR](obj: AbstractColumn[RR] with Index[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of clustering columns cannot be updated as per the Cassandra specification")
  implicit final def notClusteringKeys[T <: ClusteringOrder[RR] : ModifiableColumn, RR](obj: AbstractColumn[RR] with ClusteringOrder[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  implicit class ModifyColumnOptional[Owner <: CassandraTable[Owner, Record], Record, RR](col: OptionalColumn[Owner, Record, RR]) extends AbstractModifyColumn[Option[RR]](col.name) {

    def toCType(v: Option[RR]): AnyRef = col.toCType(v)
  }

  implicit class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  implicit class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](col: OptionalColumn[Owner, Record, T]) extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}

private [phantom] trait Operations extends ModifyImplicits with CollectionOperators with OrderingOperators with IndexRestrictions with BatchRestrictions {}
