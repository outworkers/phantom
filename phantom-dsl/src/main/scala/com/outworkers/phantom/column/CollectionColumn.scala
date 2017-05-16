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

import com.outworkers.phantom.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive

import scala.collection.generic.CanBuildFrom
import scala.util.Try

abstract class AbstractColColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  M[X] <: TraversableOnce[X],
  RR
](table: CassandraTable[Owner, Record])(
  implicit cbf: CanBuildFrom[Nothing, RR, M[RR]]
) extends Column[Owner, Record, M[RR]](table) with CollectionValueDefinition[RR] {

  override def apply(r: Row): M[RR] = parse(r).getOrElse(cbf().result())
}

class CollectionColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  M[X] <: TraversableOnce[X],
  RR
](table: CassandraTable[Owner, Record], collection: String)(
  implicit vp: Primitive[RR],
  ev: Primitive[M[RR]],
  cbf: CanBuildFrom[Nothing, RR, M[RR]]
) extends AbstractColColumn[Owner, Record, M, RR](table) {

  override def asCql(v: M[RR]): String = ev.asCql(v)

  override val cassandraType = QueryBuilder.Collections.collectionType(
    collection,
    vp.dataType,
    shouldFreeze,
    vp.frozen,
    isStaticColumn
  ).queryString

  override def parse(r: Row): Try[M[RR]] = ev.fromRow(name, r)

  override def valueAsCql(v: RR): String = vp.asCql(v)
}
