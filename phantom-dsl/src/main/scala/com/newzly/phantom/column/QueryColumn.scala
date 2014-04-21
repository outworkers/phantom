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

import com.datastax.driver.core.Row
import com.datastax.driver.core.querybuilder.{ Assignment, QueryBuilder, Clause }
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }

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