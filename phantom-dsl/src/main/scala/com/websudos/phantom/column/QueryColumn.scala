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
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.ops.UpdateClause


private[phantom] abstract class AbstractModifyColumn[RR](name: String) {

  def toCType(v: RR): AnyRef

  def setTo(value: RR): UpdateClause.Condition = QueryBuilder.set(name, toCType(value))
}


class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}

sealed abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}



sealed trait ModifyImplicits {

  /*
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
  */


  /*
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


    implicit final def notClusteringKeys[T <: ClusteringOrder[RR] : ModifiableColumn, RR]
      (obj: AbstractColumn[RR] with ClusteringOrder[RR]): ModifyColumn[RR] = new ModifyColumn(obj)
  */

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
