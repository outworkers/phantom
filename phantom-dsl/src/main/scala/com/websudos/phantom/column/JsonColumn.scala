/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.twitter.util.Try
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
}

abstract class JsonSetColumn[T <: CassandraTable[T, R], R, ValueType](table: CassandraTable[T, R]) extends AbstractSetColumn[T ,R,
  ValueType](table) with JsonDefinition[ValueType] {

  override val cassandraType = "set<text>"
}
