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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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

import scala.collection.JavaConverters._
import scala.util.{Success, Try}

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.primitives.Primitive
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.syntax.CQLSyntax

sealed trait JsonDefinition[T] {

  def fromJson(obj: String): T

  def toJson(obj: T): String

  def valueAsCql(obj: T): String = CQLQuery.empty.singleQuote(toJson(obj))

  def fromString(c: String): T = fromJson(c)

  val primitive = implicitly[Primitive[String]]
}

abstract class JsonColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends Column[T, R,
  ValueType](table) with JsonDefinition[ValueType] {

  def asCql(value: ValueType): String = CQLQuery.empty.singleQuote(toJson(value))

  val cassandraType = CQLSyntax.Types.Text

  def parse(row: Row): Try[ValueType] = {
    Try(fromJson(row.getString(name)))
  }
}

abstract class JsonListColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends AbstractListColumn[T, R,
  ValueType](table) with JsonDefinition[ValueType] {

  override def valueAsCql(obj: ValueType): String = CQLQuery.empty.singleQuote(toJson(obj))

  override val cassandraType = QueryBuilder.Collections.listType(Primitive[String].cassandraType).queryString

  override def parse(r: Row): Try[List[ValueType]] = {
    if (r.isNull(name)) {
      Success(List.empty[ValueType])
    } else {
      Success(r.getList(name, Primitive[String].clz.asInstanceOf[Class[String]]).asScala.map(fromString).toList)
    }
  }
}

abstract class JsonSetColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends AbstractSetColumn[T ,R,
  ValueType](table) with JsonDefinition[ValueType] {

  override val cassandraType = QueryBuilder.Collections.setType(Primitive[String].cassandraType).queryString

  override def parse(r: Row): Try[Set[ValueType]] = {
    if (r.isNull(name)) {
      Success(Set.empty[ValueType])
    } else {
      Success(r.getSet(name, Primitive[String].clz).asScala.map(e => fromString(e.asInstanceOf[String])).toSet[ValueType])
    }
  }
}

abstract class JsonMapColumn[Owner <: CassandraTable[Owner, Record], Record, K: Primitive, ValueType](table: CassandraTable[Owner, Record])
  extends AbstractMapColumn[Owner, Record, K, ValueType](table) with JsonDefinition[ValueType] {

  val keyPrimitive = Primitive[K]

  override def keyAsCql(v: K): String = keyPrimitive.asCql(v)

  override val cassandraType = QueryBuilder.Collections.mapType(keyPrimitive.cassandraType, Primitive[String].cassandraType).queryString

  override def qb: CQLQuery = CQLQuery(name).forcePad.append(cassandraType)

  override def keyFromCql(c: String): K = keyPrimitive.fromString(c)

  override def parse(r: Row): Try[Map[K,ValueType]] = {
    if (r.isNull(name)) {
      Success(Map.empty[K,ValueType])
    } else {
      Success(r.getMap(name, keyPrimitive.clz, Primitive[String].clz).asScala.toMap.map {
        case (k, v) => (keyPrimitive.fromPrimitive(k), fromString(v.asInstanceOf[String]))
      })
    }
  }
}
