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
import com.twitter.util.Try
import com.websudos.phantom.builder.CQLSyntax
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}

sealed trait JsonDefinition[T] {

  def fromJson(obj: String): T

  def toJson(obj: T): String

  def valueToCType(obj: T): AnyRef = toJson(obj)

  def valueFromCType(c: AnyRef): T = fromJson(c.asInstanceOf[String])

  val valueCls: Class[_] = classOf[java.lang.String]

  val primitive = implicitly[CassandraPrimitive[String]]
}

abstract class JsonColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends Column[T, R,
  ValueType](table) with JsonDefinition[ValueType] {

  def toCType(value: ValueType): AnyRef = toJson(value)

  val cassandraType = "text"

  def optional(row: Row): Option[ValueType] = {
    Try {
      fromJson(row.getString(name))
    }.toOption
  }
}


abstract class JsonListColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends AbstractListColumn[T, R,
  ValueType](table) with JsonDefinition[ValueType] {

  override val cassandraType = "list<text>"

  override def qb: CQLQuery = {
    CQLQuery(name).forcePad.append(CQLSyntax.Collections.list)
      .append(CQLSyntax.Symbols.`<`).append(CassandraPrimitive[String].cassandraType)
      .append(CQLSyntax.Symbols.`>`)
  }
}

abstract class JsonSetColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends AbstractSetColumn[T ,R,
  ValueType](table) with JsonDefinition[ValueType] {

  override val cassandraType = "set<text>"

  override def qb: CQLQuery = {
    CQLQuery(name).forcePad.append(CQLSyntax.Collections.set)
      .append(CQLSyntax.Symbols.`<`).append(CassandraPrimitive[String].cassandraType)
      .append(CQLSyntax.Symbols.`>`)
  }
}
