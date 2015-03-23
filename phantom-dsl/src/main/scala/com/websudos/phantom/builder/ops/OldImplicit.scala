package com.websudos.phantom.builder.ops

import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.column._

import scala.annotation.implicitNotFound

import com.datastax.driver.core.Row
import com.websudos.phantom.keys.{ClusteringOrder, Index, PartitionKey, PrimaryKey}
import com.websudos.phantom.CassandraTable


sealed trait ConditionalOperators extends LowPriorityImplicits {
  final implicit def columnToConditionalUpdateColumn[T](col: AbstractColumn[T]): CasConditionalOperators[T] = new CasConditionalOperators(col)
}

sealed trait OrderingOperators {
  implicit def clusteringKeyToOrderingOperator[T : Primitive](col: AbstractColumn[T] with ClusteringOrder[T]): OrderingColumn[T] = {
    new OrderingColumn[T](col)
  }
}

sealed trait IndexConversions {
  implicit def partitionColumnToIndexedColumn[T : Primitive](col: AbstractColumn[T] with PartitionKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def primaryColumnToIndexedColumn[T : Primitive](col: AbstractColumn[T] with PrimaryKey[T]): QueryColumn[T] = new QueryColumn(col)
  implicit def secondaryColumnToIndexedColumn[T : Primitive](col: AbstractColumn[T] with Index[T]): QueryColumn[T] = new QueryColumn(col)
}

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
  implicit final def nonCounterColumns[T <: CounterColumn[_, _] : ModifiableColumn, RR]
  (obj: AbstractColumn[RR] with CounterColumn[_, _]): ModifyColumn[RR] = new ModifyColumn(obj)

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
    extends AbstractModifyColumn[Option[RR]](col) {

    def toCType(v: Option[RR]): AnyRef = col.toCType(v)
  }

  class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    override def apply(r: Row): T = col.apply(r)
  }

  implicit def columnToSelection[Owner <: CassandraTable[Owner, Record], Record, T](column: Column[Owner, Record, T]): SelectColumnRequired[Owner, Record, T] = new SelectColumnRequired[Owner,
    Record, T](column)

  implicit class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](col: OptionalColumn[Owner, Record, T])
    extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}

private[phantom] trait OldImplicits extends ModifyImplicits
with CollectionOperators
with OrderingOperators
with IndexConversions
with ConditionalOperators {}
