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

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.phantom.keys.Unmodifiable
import com.outworkers.phantom.{CassandraTable, Row}

import scala.util.Try

private[phantom] trait CounterRestriction[T]

class CounterColumn[
  Owner <: CassandraTable[Owner, Record],
  Record
](table: CassandraTable[Owner, Record])(implicit primitive: Primitive[Long])
  extends Column[Owner, Record, Long](table) with CounterRestriction[Long] with Unmodifiable {

  val cassandraType = CQLSyntax.Types.Counter

  override val isCounterColumn = true

  def parse(r: Row): Try[Long] = primitive.fromRow(name, r) recover {
    case e: Exception if r.isNull(name) => 0L
  }

  override def asCql(v: Long): String = primitive.asCql(v)
}
