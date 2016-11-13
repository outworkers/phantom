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

import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.QueryBuilder.Utils
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.CQLQuery

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import scala.util.{Success, Try}


abstract class AbstractListColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  RR
](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, List[RR]](table)
  with CollectionValueDefinition[RR] {

  override def asCql(v: List[RR]): String = QueryBuilder.Collections.serialize(v.map(valueAsCql)).queryString

  override def apply(r: Row): List[RR] = {
    parse(r).getOrElse(Nil)
  }
}

class ListColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  RR : Primitive
](table: CassandraTable[Owner, Record])
    extends AbstractListColumn[Owner, Record, RR](table) with PrimitiveCollectionValue[RR] {

  override val valuePrimitive = Primitive[RR]

  override val cassandraType = QueryBuilder.Collections.listType(valuePrimitive.cassandraType).queryString

  override def qb: CQLQuery = {
    if (shouldFreeze) {
      QueryBuilder.Collections.frozen(name, cassandraType)
    } else if (valuePrimitive.frozen) {
      CQLQuery(name).forcePad.append(QueryBuilder.Collections.frozen(valuePrimitive.cassandraType))
    } else {
      CQLQuery(name).forcePad.append(cassandraType)
    }
  }

  override def valueAsCql(v: RR): String = valuePrimitive.asCql(v)

  override def fromString(value: String): RR = valuePrimitive.fromString(value)

  override def parse(r: Row): Try[List[RR]] = {
    if (r.isNull(name)) {
      Success(Nil)
    } else {
      Success(r.getList(name, Primitive[RR].clz.asInstanceOf[Class[RR]]).asScala.toList)
    }
  }
}
