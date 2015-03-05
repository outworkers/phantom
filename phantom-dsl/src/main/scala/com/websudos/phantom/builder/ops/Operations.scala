package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.column._

import scala.annotation.implicitNotFound
import scala.runtime._
import scala.collection.{ mutable, immutable, generic }
import mutable.WrappedArray
import immutable.WrappedString
import generic.CanBuildFrom

import com.datastax.driver.core.Row
import com.websudos.phantom.keys.{ClusteringOrder, Index, PartitionKey, PrimaryKey}
import com.websudos.phantom.query.{QueryCondition, QueryOrdering, SecondaryQueryCondition}
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}


sealed class OrderingColumn[T](col: AbstractColumn[T]) {
  def asc: QueryOrdering = {
    QueryOrdering(QueryBuilder.asc(col.name))
  }

  def desc: QueryOrdering = {
    QueryOrdering(QueryBuilder.desc(col.name))
  }
}

sealed abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}

class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}

class QueryColumn[RR : CassandraPrimitive](col: AbstractColumn[RR]) extends AbstractQueryColumn[RR](col)

class ConditionalOperations[T](val col: AbstractColumn[T]) {

  /**
   * The equals operator. Will return a match if the value equals the database value.
   * @param value The value to search for in the database.
   * @return A QueryCondition, wrapping a QueryBuilder clause.
   */
  def eqs(value: T): SecondaryQueryCondition = {
    SecondaryQueryCondition(QueryBuilder.eqs(col.name, col.toCType(value)))
  }
}


sealed trait ConditionalOperators extends LowPriorityImplicits {
  final implicit def columnToConditionalUpdateColumn[T](col: AbstractColumn[T]): ConditionalOperations[T] = new ConditionalOperations(col)
}

sealed trait CollectionOperators {


  implicit class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractListColumn[Owner, Record, RR])
    extends ModifyColumn[List[RR]](col) {

    def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, col.valueToCType(value))
    def prependAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.prependAll(col.name, col.valuesToCType(values))
    def append(value: RR): Assignment = QueryBuilder.append(col.name, col.valueToCType(value))
    def appendAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.appendAll(col.name, col.valuesToCType(values))
    def discard(value: RR): Assignment = QueryBuilder.discard(col.name, col.valueToCType(value))
    def discardAll[L](values: L)(implicit ev1: L => Seq[RR]): Assignment = QueryBuilder.discardAll(col.name, col.valuesToCType(values))
    def setIdx(i: Int, value: RR): Assignment = QueryBuilder.setIdx(col.name, i, col.valueToCType(value))
  }

  implicit class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractSetColumn[Owner, Record, RR])
    extends ModifyColumn[Set[RR]](col) {

    def add(value: RR): Assignment = QueryBuilder.add(col.name, col.valueToCType(value))
    def addAll(values: Set[RR]): Assignment = QueryBuilder.addAll(col.name, col.valuesToCType(values))
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, col.valueToCType(value))
    def removeAll(values: Set[RR]): Assignment = QueryBuilder.removeAll(col.name, col.valuesToCType(values))
  }

  implicit class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A, B](col: AbstractMapColumn[Owner, Record, A, B])
    extends ModifyColumn[Map[A, B]](col) {

    def put(value: (A, B)): Assignment = QueryBuilder.put(col.name, col.keyToCType(value._1), col.valueToCType(value._2))
    def putAll[L](values: L)(implicit ev1: L => Traversable[(A, B)]): Assignment = QueryBuilder.putAll(col.name, col.valuesToCType(values))
  }
}

sealed trait OrderingOperators {
  implicit def clusteringKeyToOrderingOperator[T : CassandraPrimitive](col: AbstractColumn[T] with ClusteringOrder[T]): OrderingColumn[T] = {
    new OrderingColumn[T](col)
  }
}

sealed trait IndexRestrictions {
  implicit def partitionColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with PartitionKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def primaryColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with PrimaryKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def secondaryColumnToIndexedColumn[T : CassandraPrimitive](col: AbstractColumn[T] with Index[T]): QueryColumn[T] = new QueryColumn(col)
}

sealed class ModifiableColumn[T]
sealed trait ModifyImplicits extends LowPriorityImplicits {

  implicit final def columnsAreModifiable[T <: AbstractColumn[_]]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def countersAreNotModifiable[T <: AbstractColumn[RR] with CounterRestriction[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def countersAreNotModifiable2[T <: AbstractColumn[RR] with CounterRestriction[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def primaryKeysAreNotModifiable[T <: AbstractColumn[RR] with PrimaryKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def primaryKeysAreNotModifiable2[T <: AbstractColumn[RR] with PrimaryKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def clusteringKeysAreNotModifiable[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def clusteringKeysAreNotModifiable2[T <: AbstractColumn[RR] with ClusteringOrder[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def partitionKeysAreNotModifiable[T <: AbstractColumn[RR] with PartitionKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def partitionKeysAreNotModifiable2[T <: AbstractColumn[RR] with PartitionKey[RR], RR]: ModifiableColumn[T] = new ModifiableColumn
  implicit final def indexesAreNotModifiable[T <: AbstractColumn[RR] with Index[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]
  implicit final def indexesAreNotModifiable2[T <: AbstractColumn[RR] with Index[RR], RR]: ModifiableColumn[T] = new ModifiableColumn[T]

  implicit final def columnToModifyColumn[T](col: AbstractColumn[T]): ModifyColumn[T] = new ModifyColumn[T](col)

  @implicitNotFound(msg = "CounterColumns can only be incremented or decremented.")
  implicit final def nonCounterColumns[T <: CounterRestriction[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with CounterRestriction[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of primary key columns cannot be updated as per the Cassandra specification")
  implicit final def notPrimaryKeys[T <: PrimaryKey[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with PrimaryKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of partition key columns cannot be updated as per the Cassandra specification")
  implicit final def notPartitionKeys[T <: PartitionKey[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with PartitionKey[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of indexed columns cannot be updated as per the Cassandra specification")
  implicit final def notIndexKeys[T <: PartitionKey[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with Index[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  @implicitNotFound(msg = "The value of clustering columns cannot be updated as per the Cassandra specification")
  implicit final def notClusteringKeys[T <: ClusteringOrder[RR] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with ClusteringOrder[RR]): ModifyColumn[RR] = new ModifyColumn(obj)

  implicit class ModifyColumnOptional[Owner <: CassandraTable[Owner, Record], Record, RR](col: OptionalColumn[Owner, Record, RR])
    extends AbstractModifyColumn[Option[RR]](col.name) {

    def toCType(v: Option[RR]): AnyRef = col.toCType(v)
  }

  class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  implicit def columnToSelection[Owner <: CassandraTable[Owner, Record], Record, T](column: Column[Owner, Record, T]): SelectColumnRequired[Owner, Record, T] = new SelectColumnRequired[Owner,
    Record, T](column)

  implicit class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](col: OptionalColumn[Owner, Record, T])
    extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}

private[phantom] trait Operations extends ModifyImplicits
  with CollectionOperators
  with OrderingOperators
  with IndexRestrictions
  with ConditionalOperators
