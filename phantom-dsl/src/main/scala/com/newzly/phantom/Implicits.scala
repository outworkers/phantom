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
import org.joda.time.DateTime
import com.datastax.driver.core.querybuilder.QueryBuilder
import com.newzly.phantom.column._
import com.newzly.phantom.query.{SelectQuery, SelectWhere, QueryCondition}

object Implicits extends Operations {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.CassandraTable[Owner, Record]
  type BatchStatement = com.newzly.phantom.batch.BatchStatement
  type CounterBatchStatement = com.newzly.phantom.batch.CounterBatchStatement
  type UnloggedBatchStatement = com.newzly.phantom.batch.UnloggedBatchStatement

  val BatchStatement = com.newzly.phantom.batch.BatchStatement
  val CounterBatchStatement = com.newzly.phantom.batch.CounterBatchStatement
  val UnloggedBatchStatement = com.newzly.phantom.batch.UnloggedBatchStatement

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
  type Ascending = com.newzly.phantom.keys.Ascending
  type Descending = com.newzly.phantom.keys.Descending
  type PartitionKey[ValueType] = com.newzly.phantom.keys.PartitionKey[ValueType]
  type PrimaryKey[ValueType] = com.newzly.phantom.keys.PrimaryKey[ValueType]
  type Index[ValueType] = com.newzly.phantom.keys.Index[ValueType]
  type StaticColumn[ValueType] = com.newzly.phantom.keys.StaticColumn[ValueType]
  type LongOrderKey[Owner <: CassandraTable[Owner, Record], Record] = com.newzly.phantom.keys.LongOrderKey[Owner, Record]


  implicit class SkipSelect[T <: CassandraTable[T, R] with LongOrderKey[T, R], R](val select: SelectQuery[T, R]) extends AnyVal {
    final def skip(l: Int): SelectWhere[T, R] = {
      select.where(_.order_id gt l.toLong)
    }

    final def skip(l: Long): SelectWhere[T, R] = {
      select.where(_.order_id gt l)
    }
  }

  implicit class SkipSelectWhere[T <: CassandraTable[T, R] with LongOrderKey[T, R], R](val select: SelectWhere[T, R]) extends AnyVal {
    final def skip(l: Int): SelectWhere[T, R] = {
      select.and(_.order_id gt l.toLong)
    }

    final def skip(l: Long): SelectWhere[T, R] = {
      select.and(_.order_id gt l)
    }
  }

  implicit class PartitionTokenHelper[T](val p: AbstractColumn[T] with PartitionKey[T]) extends AnyVal {

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
