/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.column

import com.datastax.driver.core.Row
import com.websudos.phantom.{CassandraPrimitive, CassandraTable}

class EnumColumn[Owner <: CassandraTable[Owner, Record], Record, EnumType <: Enumeration](table: CassandraTable[Owner, Record], enum: EnumType)
  extends Column[Owner, Record, EnumType#Value](table) {

  def toCType(v: EnumType#Value): AnyRef = v.toString

  def cassandraType: String = CassandraPrimitive[String].cassandraType

  def optional(r: Row): Option[EnumType#Value] =
    Option(r.getString(name)).flatMap(s => enum.values.find(_.toString == s))
}

class OptionalEnumColumn[Owner <: CassandraTable[Owner, Record], Record, EnumType <: Enumeration](table: CassandraTable[Owner, Record], enum: EnumType)
  extends OptionalColumn[Owner, Record, EnumType#Value](table) {

  def cassandraType: String = CassandraPrimitive[String].cassandraType

  def optional(r: Row): Option[EnumType#Value] =
    Option(r.getString(name)).flatMap(s => enum.values.find(_.toString == s))

  override def toCType(v: Option[EnumType#Value]): AnyRef = v.map(_.toString).orNull
}
