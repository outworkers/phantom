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

abstract class AbstractQueryColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: Column[Owner, Record, RR]) {

  val primitive = implicitly[CassandraPrimitive[RR]]
  def eqs(value: RR): Clause = QueryBuilder.eq(col.name, primitive.toCType(value))
  def in[L <% Traversable[RR]](vs: L) = QueryBuilder.in(col.name, vs.map(primitive.toCType).toSeq: _*)
  def gt(value: RR): Clause = QueryBuilder.gt(col.name, primitive.toCType(value))
  def gte(value: RR): Clause = QueryBuilder.gte(col.name, primitive.toCType(value))
  def lt(value: RR): Clause = QueryBuilder.lt(col.name, primitive.toCType(value))
  def lte(value: RR): Clause = QueryBuilder.lte(col.name, primitive.toCType(value))
}

class QueryColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: Column[Owner, Record, RR]) extends AbstractQueryColumn[Owner, Record, RR](col)

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

class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: ListColumn[Owner, Record, RR]) extends ModifyColumn[List[RR]](col) {

  def prepend(value: RR): Assignment = QueryBuilder.prepend(col.name, CassandraPrimitive[RR].toCType(value))
  def prependAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.prependAll(col.name, values.map(CassandraPrimitive[RR].toCType).toList.asJava)
  def append(value: RR): Assignment = QueryBuilder.append(col.name, CassandraPrimitive[RR].toCType(value))
  def appendAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.appendAll(col.name, values.map(CassandraPrimitive[RR].toCType).toList.asJava)
  def remove(value: RR): Assignment = QueryBuilder.remove(col.name, CassandraPrimitive[RR].toCType(value))
  def removeAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.removeAll(col.name, values.map(CassandraPrimitive[RR].toCType).toSet.asJava)
}

class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](col: SetColumn[Owner, Record, RR]) extends ModifyColumn[Set[RR]](col) {

  def add(value: RR): Assignment = QueryBuilder.add(col.name, CassandraPrimitive[RR].toCType(value))
  def addAll[L <% Traversable[RR]](values: L): Assignment = QueryBuilder.addAll(col.name, values.map(CassandraPrimitive[RR].toCType).toSet.asJava)
  def remove(value: RR): Assignment = QueryBuilder.remove(col.name, CassandraPrimitive[RR].toCType(value))
  def removeAll[L <% Seq[RR]](values: L): Assignment = QueryBuilder.removeAll(col.name, values.map(CassandraPrimitive[RR].toCType).toSet.asJava)
}

class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A: CassandraPrimitive, B: CassandraPrimitive](col: MapColumn[Owner, Record, A, B]) extends ModifyColumn[Map[A, B]](col) {

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