/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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

import com.outworkers.phantom.{CassandraTable, Row}
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.clauses.DeleteClause
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.prepared.PrepareMark
import com.outworkers.phantom.connectors.KeySpace
import shapeless.{::, HNil}

import scala.util.Try
import scala.collection.compat._

abstract class AbstractColColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  M[X] <: IterableOnce[X],
  RR
](table: CassandraTable[Owner, Record])(
  implicit cbf: Factory[RR, M[RR]]
) extends Column[Owner, Record, M[RR]](table) with CollectionValueDefinition[RR] {

  override def apply(r: Row): M[RR] = parse(r).getOrElse(cbf.newBuilder.result())
}

class CollectionColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  M[X] <: IterableOnce[X],
  RR
](table: CassandraTable[Owner, Record], collection: String)(
  implicit vp: Primitive[RR],
  ev: Primitive[M[RR]],
  cbf: Factory[RR, M[RR]]
) extends AbstractColColumn[Owner, Record, M, RR](table) {

  override def asCql(v: M[RR]): String = ev.asCql(v)

  override val cassandraType: String = QueryBuilder.Collections.collectionType(
    collection,
    vp.dataType,
    shouldFreeze,
    vp.frozen,
    isStaticColumn
  ).queryString

  override def parse(r: Row): Try[M[RR]] = ev.fromRow(name, r)

  override def valueAsCql(v: RR): String = vp.asCql(v)

  final def apply(mark: PrepareMark)(implicit space: KeySpace): DeleteClause.Prepared[RR] = {
    new DeleteClause.Condition[RR :: HNil](
      QueryBuilder.Delete.deletePrepared(table.tableName, this.name, mark)
    )
  }
}
