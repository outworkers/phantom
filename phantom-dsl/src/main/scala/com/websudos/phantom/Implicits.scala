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
package com.websudos.phantom


import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.Date

import com.datastax.driver.core.querybuilder.QueryBuilder
import com.datastax.driver.core.{ConsistencyLevel => CLevel}
import com.websudos.phantom.column.{AbstractColumn, Operations}
import com.websudos.phantom.query.{QueryCondition, SelectQuery, SelectWhere}
import org.joda.time.DateTime

@deprecated("Use import com.websudos.phantom.dsl._ instead of importing Implicits", "1.6.0")
object Implicits extends Operations {

  type CassandraTable[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.CassandraTable[Owner, Record]
  type BatchStatement = com.websudos.phantom.batch.BatchStatement
  type CounterBatchStatement = com.websudos.phantom.batch.CounterBatchStatement
  type UnloggedBatchStatement = com.websudos.phantom.batch.UnloggedBatchStatement

  val BatchStatement = com.websudos.phantom.batch.BatchStatement
  val CounterBatchStatement = com.websudos.phantom.batch.CounterBatchStatement
  val UnloggedBatchStatement = com.websudos.phantom.batch.UnloggedBatchStatement

  type Column[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.Column[Owner, Record, T]
  type PrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.websudos.phantom.column.PrimitiveColumn[Owner, Record, T]
  type OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.websudos.phantom.column.OptionalColumn[Owner, Record, T]

  type OptionalPrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, T]
  type ListColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.ListColumn[Owner, Record, T]
  type SetColumn[Owner <: CassandraTable[Owner, Record], Record, T] =  com.websudos.phantom.column.SetColumn[Owner, Record, T]
  type MapColumn[Owner <: CassandraTable[Owner, Record], Record, K, V] =  com.websudos.phantom.column.MapColumn[Owner, Record, K, V]
  type JsonColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.JsonColumn[Owner, Record, T]
  type EnumColumn[Owner <: CassandraTable[Owner, Record], Record, T <: Enumeration] = com.websudos.phantom.column.EnumColumn[Owner, Record, T]
  type OptionalEnumColumn[Owner <: CassandraTable[Owner, Record], Record, T <: Enumeration] = com.websudos.phantom.column.OptionalEnumColumn[Owner, Record, T]

  type JsonSetColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.JsonSetColumn[Owner, Record, T]
  type JsonListColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.JsonListColumn[Owner, Record, T]
  type BigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, BigDecimal]

  type BlobColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, ByteBuffer]
  type BigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, BigInt]
  type BooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, Boolean]
  type DateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.DateColumn[Owner, Record]
  type DateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.DateTimeColumn[Owner, Record]
  type DoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, Double]
  type FloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, Float]
  type IntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, Int]
  type InetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, InetAddress]
  type LongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, Long]
  type StringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, String]
  type UUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.PrimitiveColumn[Owner, Record, UUID]
  type CounterColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.CounterColumn[Owner, Record]
  type TimeUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.TimeUUIDColumn[Owner, Record]

  type OptionalBlobColumn[Owner <: CassandraTable[Owner, Record], Record, T] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, ByteBuffer]
  type OptionalBigDecimalColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigDecimal]
  type OptionalBigIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, BigInt]
  type OptionalBooleanColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Boolean]
  type OptionalDateColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Date]
  type OptionalDateTimeColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, DateTime]
  type OptionalDoubleColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Double]
  type OptionalFloatColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Float]
  type OptionalIntColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Int]
  type OptionalInetAddressColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, InetAddress]
  type OptionalLongColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, Long]
  type OptionalStringColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, String]
  type OptionalUUIDColumn[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.column.OptionalPrimitiveColumn[Owner, Record, UUID]

  type ClusteringOrder[ValueType] = com.websudos.phantom.keys.ClusteringOrder[ValueType]
  type Ascending = com.websudos.phantom.keys.Ascending
  type Descending = com.websudos.phantom.keys.Descending
  type PartitionKey[ValueType] = com.websudos.phantom.keys.PartitionKey[ValueType]
  type PrimaryKey[ValueType] = com.websudos.phantom.keys.PrimaryKey[ValueType]
  type Index[ValueType] = com.websudos.phantom.keys.Index[ValueType]
  type StaticColumn[ValueType] = com.websudos.phantom.keys.StaticColumn[ValueType]
  type LongOrderKey[Owner <: CassandraTable[Owner, Record], Record] = com.websudos.phantom.keys.LongOrderKey[Owner, Record]

  type SimpleCassandraConnector = com.websudos.phantom.connectors.SimpleCassandraConnector
  type CassandraConnector = com.websudos.phantom.connectors.CassandraConnector
  type CassandraManager = com.websudos.phantom.connectors.CassandraManager

  type UUID = java.util.UUID
  type Row = com.datastax.driver.core.Row
  type ResultSet = com.datastax.driver.core.ResultSet
  type Session = com.datastax.driver.core.Session


  object ConsistencyLevel {
    val ALL = CLevel.ALL
    val Any = CLevel.ANY
    val ONE = CLevel.ONE
    val TWO = CLevel.TWO
    val THREE = CLevel.THREE
    val QUORUM = CLevel.QUORUM
    val LOCAL_QUORUM = CLevel.LOCAL_QUORUM
    val EACH_QUORUM = CLevel.EACH_QUORUM
    val LOCAL_SERIAL = CLevel.LOCAL_SERIAL
    val LOCAL_ONE = CLevel.LOCAL_ONE
    val SERIAL = CLevel.SERIAL
  }


  implicit class SkipSelect[T <: CassandraTable[T, R] with LongOrderKey[T, R], R](val select: SelectQuery[T, R]) extends AnyVal {
    final def skip(l: Int): SelectWhere[T, R] = {
      select.where(_.orderId gt l.toLong)
    }

    final def skip(l: Long): SelectWhere[T, R] = {
      select.where(_.orderId gt l)
    }
  }

  implicit class SkipSelectWhere[T <: CassandraTable[T, R] with LongOrderKey[T, R], R](val select: SelectWhere[T, R]) extends AnyVal {
    final def skip(l: Int): SelectWhere[T, R] = {
      select.and(_.orderId gt l.toLong)
    }

    final def skip(l: Long): SelectWhere[T, R] = {
      select.and(_.orderId gt l)
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
