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

import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Clause }
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.newzly.phantom.keys.{ Index, PrimaryKey, PartitionKey }

abstract class AbstractQueryColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: Column[Owner, Record, RR]) {

  val primitive = implicitly[CassandraPrimitive[RR]]
  def eqs(value: RR): Clause = QueryBuilder.eq(col.name, primitive.toCType(value))
  def in[L <% Traversable[RR]](vs: L) = QueryBuilder.in(col.name, vs.map(primitive.toCType).toSeq: _*)
  def gt(value: RR): Clause = QueryBuilder.gt(col.name, primitive.toCType(value))
  def gte(value: RR): Clause = QueryBuilder.gte(col.name, primitive.toCType(value))
  def lt(value: RR): Clause = QueryBuilder.lt(col.name, primitive.toCType(value))
  def lte(value: RR): Clause = QueryBuilder.lte(col.name, primitive.toCType(value))
}


abstract class AbstractModifyColumn[RR](name: String) {

  def toCType(v: RR): AnyRef

  def setTo(value: RR): Assignment = QueryBuilder.set(name, toCType(value))
}

class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col.name) {

  def toCType(v: RR): AnyRef = col.toCType(v)
}

class ModifyColumnOptional[Owner <: CassandraTable[Owner, Record], Record, RR](col: OptionalColumn[Owner, Record, RR]) extends AbstractModifyColumn[Option[RR]](col.name) {

  def toCType(v: Option[RR]): AnyRef = col.toCType(v)
}

abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}

class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](override val col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
  def apply(r: Row): T = col.apply(r)
}

class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](override val col: OptionalColumn[Owner, Record, T]) extends SelectColumn[Option[T]](col) {
  def apply(r: Row): Option[T] = col.apply(r)
}

sealed trait CollectionOperators {

  implicit class CounterModifyColumn[Owner <: CassandraTable[Owner, Record], Record](col: CounterColumn[Owner, Record]) {
    def increment(): Assignment = QueryBuilder.incr(col.name, 1L)
    def increment(value: Long): Assignment = QueryBuilder.incr(col.name, value)
    def decrement(): Assignment = QueryBuilder.decr(col.name)
    def decrement(value: Long): Assignment = QueryBuilder.decr(col.name, value)
  }

  implicit class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: ListColumn[Owner, Record, RR]) extends ModifyColumn[List[RR]](col) {

    def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, CassandraPrimitive[RR].toCType(value))
    def prependAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.prependAll(col.name, values.map(CassandraPrimitive[RR].toCType).toList.asJava)
    def append(value: RR): Assignment = QueryBuilder.append(col.name, CassandraPrimitive[RR].toCType(value))
    def appendAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.appendAll(col.name, values.map(CassandraPrimitive[RR].toCType).toList.asJava)
    def discard(value: RR): Assignment = QueryBuilder.discard(col.name, CassandraPrimitive[RR].toCType(value))
    def discardAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.discardAll(col.name, values.map(CassandraPrimitive[RR].toCType).asJava)
    def setIdx(i: Int, value: RR): Assignment = QueryBuilder.setIdx(col.name, i, CassandraPrimitive[RR].toCType(value))
  }

  implicit class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: SetColumn[Owner, Record, RR]) extends ModifyColumn[Set[RR]](col) {

    def add(value: RR): Assignment = QueryBuilder.add(col.name, CassandraPrimitive[RR].toCType(value))
    def addAll(values: Set[RR]): Assignment = QueryBuilder.addAll(col.name, values.map(CassandraPrimitive[RR].toCType).toSet.asJava)
    def remove(value: RR): Assignment = QueryBuilder.remove(col.name, CassandraPrimitive[RR].toCType(value))
    def removeAll(values: Set[RR]): Assignment = QueryBuilder.removeAll(col.name, values.map(CassandraPrimitive[RR].toCType).toSet.asJava)
  }

  implicit class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A: CassandraPrimitive, B: CassandraPrimitive](col: MapColumn[Owner, Record, A, B]) extends ModifyColumn[Map[A, B]](col) {

    def put(value: (A, B)): Assignment = QueryBuilder.put(col.name, CassandraPrimitive[A].toCType(value._1), CassandraPrimitive[B].toCType(value._2))
    def putAll[L <% Traversable[(A, B)]](values: L): Assignment = {
      val map = values.map({ case (k, v) => CassandraPrimitive[A].toCType(k) -> CassandraPrimitive[B].toCType(v) }).toMap.asJava
      QueryBuilder.putAll(col.name, map)
    }
  }
}

sealed trait IndexRestrictions {
  implicit def partitionColumnToIndexedColumn[T](col: AbstractColumn[T] with PartitionKey[T]): IndexedColumn[T] = new IndexedColumn[T](col)
  implicit def primaryColumnToIndexedColumn[T](col: AbstractColumn[T] with PrimaryKey[T]): IndexedColumn[T] = new IndexedColumn[T](col)
  implicit def secondaryColumnToIndexedColumn[T](col: AbstractColumn[T] with Index[T]): IndexedColumn[T] = new IndexedColumn[T](col)
}

sealed trait ModifyImplicits extends LowPriorityImplicits {
  trait =!=[A, B]
  type ¬[T] = T => Nothing
  implicit def neg[T, U](t : T)(implicit ev : T =!= U) : ¬[U] = null
  final def notCounter[T <: AbstractColumn[_] <% ¬[CounterRestriction[_]]](t : T) = t

  implicit def simpleColumnToAssignment[RR](col: AbstractColumn[RR]) : ModifyColumn[RR] = {
    new ModifyColumn[RR](col)
  }

  implicit def simpleOptionalColumnToAssignment[T <: CassandraTable[T, R], R, RR: CassandraPrimitive](col: OptionalColumn[T, R, RR]) = {
    new ModifyColumnOptional[T, R, RR](col)
  }

  implicit def columnIsSelectable[T <: CassandraTable[T, R], R, RR](col: Column[T, R, RR]): SelectColumn[RR] =
    new SelectColumnRequired[T, R, RR](col)

  implicit def optionalColumnIsSelectable[T <: CassandraTable[T, R], R, RR](col: OptionalColumn[T, R, RR]): SelectColumn[Option[RR]] =
    new SelectColumnOptional[T, R, RR](col)
}

private [phantom] trait Operations extends ModifyImplicits with CollectionOperators with IndexRestrictions {}
