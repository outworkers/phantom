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
package com.newzly.phantom


import java.net.InetAddress
import java.util.{ Date, UUID }
import scala.collection.JavaConverters._
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder }
import com.newzly.phantom.column._
import com.newzly.phantom.query.{ QueryCondition, SelectWhere }
import com.newzly.phantom.query.QueryCondition

object Implicits {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.CassandraTable[Owner, Record]

  type Column[Owner <: CassandraTable[Owner, Record], Record, T] = com.newzly.phantom.column.Column[Owner, Record, T]
  type PrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.newzly.phantom.column.PrimitiveColumn[Owner, Record, T]

  type OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.newzly.phantom.column.OptionalColumn[Owner, Record, T]
  type OptionalPrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, T]
  type ListColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.newzly.phantom.column.ListColumn[Owner, Record, T]
  type SetColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.newzly.phantom.column.SetColumn[Owner, Record, T]
  type MapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V] =  com.newzly.phantom.column.MapColumn[Owner, Record, K, V]


  type BigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, BigDecimal]
  type BigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, BigInt]
  type BooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, Boolean]
  type DateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.DateColumn[Owner, Record]
  type DateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.DateTimeColumn[Owner, Record]
  type DoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, Double]
  type FloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, Float]
  type IntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, Int]
  type InetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, InetAddress]
  type LongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, Long]
  type StringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, String]
  type UUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.PrimitiveColumn[Owner, Record, UUID]
  type CounterColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.CounterColumn[Owner, Record]
  type TimeUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.TimeUUIDColumn[Owner, Record]

  type OptionalBigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigDecimal]
  type OptionalBigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigInt]
  type OptionalBooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Boolean]
  type OptionalDateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Date]
  type OptionalDateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, DateTime]
  type OptionalDoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Double]
  type OptionalFloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Float]
  type OptionalIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Int]
  type OptionalInetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, InetAddress]
  type OptionalLongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, Long]
  type OptionalStringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, String]
  type OptionalUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.column.OptionalPrimitiveColumn[Owner, Record, UUID]

  type ClusteringOrder[ValueType] = com.newzly.phantom.keys.ClusteringOrder[ValueType]
  type PartitionKey[ValueType] = com.newzly.phantom.keys.PartitionKey[ValueType]
  type PrimaryKey[ValueType] = com.newzly.phantom.keys.PrimaryKey[ValueType]
  type Index[ValueType] = com.newzly.phantom.keys.Index[ValueType]
  type StaticColumn[ValueType] = com.newzly.phantom.keys.StaticColumn[ValueType]
  type LongOrderKey[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.keys.LongOrderKey[Owner, Record]


  implicit class CounterModifyColumn[Owner <: CassandraTable[Owner, Record], Record](col: CounterColumn[Owner, Record]) extends ModifyColumn[Long](col) {
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

  class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](override val col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](override val col: OptionalColumn[Owner, Record, T]) extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }

  implicit def partitionColumnToIndexedColumn[T](col: AbstractColumn[T] with PartitionKey[T]): IndexedColumn[T] = new IndexedColumn[T](col)
  implicit def primaryColumnToIndexedColumn[T](col: AbstractColumn[T] with PrimaryKey[T]): IndexedColumn[T] = new IndexedColumn[T](col)
  implicit def secondaryColumnToIndexedColumn[T](col: AbstractColumn[T] with Index[T]): IndexedColumn[T] = new IndexedColumn[T](col)

  implicit def simpleColumnToAssignment[RR: CassandraPrimitive](col: AbstractColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def simpleOptionalColumnToAssignment[T <: CassandraTable[T, R], R, RR: CassandraPrimitive](col: OptionalColumn[T, R, RR]) = {
    new ModifyColumnOptional[T, R, RR](col)
  }

  implicit def columnIsSelectable[T <: CassandraTable[T, R], R, RR](col: Column[T, R, RR]): SelectColumn[RR] =
    new SelectColumnRequired[T, R, RR](col)

  implicit def optionalColumnIsSelectable[T <: CassandraTable[T, R], R, RR](col: OptionalColumn[T, R, RR]): SelectColumn[Option[RR]] =
    new SelectColumnOptional[T, R, RR](col)

  implicit class SkipSelect[T <: CassandraTable[T, R] with LongOrderKey[T, R], R](val select: SelectWhere[T, R]) extends AnyVal {
    final def skip(l: Int): SelectWhere[T, R] = {
      select.where(_.order_id gt l.toLong)
    }

    final def skip(l: Long): SelectWhere[T, R] = {
      select.where(_.order_id gt l)
    }
  }

  implicit class PartitionTokenHelper[T](val p: PartitionKey[T]) extends AnyVal {

    def ltToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.lt(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token", p.asInstanceOf[Column[_, _, T]].toCType(value))))
    }

    def lteToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.lte(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token", p.asInstanceOf[Column[_, _, T]].toCType(value))))
    }

    def gtToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.gt(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token", p.asInstanceOf[Column[_, _, T]].toCType(value))))
    }

    def gteToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.gte(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token", p.asInstanceOf[Column[_, _, T]].toCType(value))))
    }

    def eqsToken (value: T): QueryCondition = {
      QueryCondition(QueryBuilder.eq(QueryBuilder.token(p.asInstanceOf[Column[_,_,T]].name),
        QueryBuilder.fcall("token", p.asInstanceOf[Column[_, _, T]].toCType(value))))
    }
  }

  implicit lazy val context = Manager.scalaExecutor

}
