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
package com.outworkers.phantom.column

import scala.collection.JavaConverters._
import scala.util.{Success, Try}
import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

sealed trait JsonDefinition[T] {

  def fromJson(obj: String): T

  def toJson(obj: T): String

  def valueAsCql(obj: T): String = CQLQuery.empty.singleQuote(toJson(obj))

  def fromString(c: String): T = fromJson(c)
}

abstract class JsonColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R]) extends Column[T, R, ValueType](table) with JsonDefinition[ValueType] {

  def asCql(value: ValueType): String = CQLQuery.empty.singleQuote(toJson(value))

  val cassandraType = CQLSyntax.Types.Text

  def parse(row: Row): Try[ValueType] = {
    Try(fromJson(row.getString(name)))
  }
}

abstract class OptionalJsonColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R]) extends OptionalColumn[T, R, ValueType](table) with JsonDefinition[ValueType] {

  def asCql(value: Option[ValueType]): String = value match {
    case Some(json) => CQLQuery.empty.singleQuote(toJson(json))
    case None => CQLQuery.empty.singleQuote("")
  }

  val cassandraType = CQLSyntax.Types.Text

  override def optional(r: Row): Try[ValueType] = Try(fromJson(r.getString(name)))

}

abstract class JsonListColumn[
  T <: CassandraTable[T, R],
  R,
  ValueType
](table: CassandraTable[T, R])(
  implicit primitive: Primitive[String],
  ev: Primitive[List[String]]
) extends AbstractListColumn[T, R, ValueType](table) with JsonDefinition[ValueType] {

  override def valueAsCql(obj: ValueType): String = CQLQuery.empty.singleQuote(toJson(obj))

  override val cassandraType = QueryBuilder.Collections.listType(primitive.cassandraType).queryString

  override def parse(r: Row): Try[List[ValueType]] = {
    if (r.isNull(name)) {
      Success(List.empty[ValueType])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name)).map(fromString))
    }
  }
}

abstract class JsonSetColumn[T <: CassandraTable[T, R], R, ValueType](
  table: CassandraTable[T, R]
)(
  implicit primitive: Primitive[String],
  ev: Primitive[Set[String]]
) extends AbstractSetColumn[T ,R,
  ValueType](table) with JsonDefinition[ValueType] {

  override val cassandraType = QueryBuilder.Collections.setType(primitive.cassandraType).queryString

  override def parse(r: Row): Try[Set[ValueType]] = {
    if (r.isNull(name)) {
      Success(Set.empty[ValueType])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name)).map(fromString))
    }
  }
}

abstract class JsonMapColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  KeyType,
  ValueType
](table: CassandraTable[Owner, Record])(
  implicit keyPrimitive: Primitive[KeyType],
  strPrimitive: Primitive[String],
  ev: Primitive[Map[KeyType, String]]
) extends AbstractMapColumn[Owner, Record, KeyType, ValueType](table) with JsonDefinition[ValueType] {

  override def keyAsCql(v: KeyType): String = keyPrimitive.asCql(v)

  override val cassandraType = QueryBuilder.Collections.mapType(
    keyPrimitive.cassandraType,
    strPrimitive.cassandraType
  ).queryString

  override def qb: CQLQuery = CQLQuery(name).forcePad.append(cassandraType)

  override def keyFromCql(c: String): KeyType = keyPrimitive.fromString(c)

  override def parse(r: Row): Try[Map[KeyType,ValueType]] = {
    if (r.isNull(name)) {
      Success(Map.empty[KeyType, ValueType])
    } else {
      Try(ev.deserialize(r.getBytesUnsafe(name)).mapValues(fromString))
    }
  }
}
