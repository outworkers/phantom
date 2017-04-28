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

import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.syntax.CQLSyntax

import scala.annotation.implicitNotFound
import scala.util.{Failure, Success, Try}

abstract class AbstractSetColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
  extends Column[Owner, Record, Set[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): Set[String] = values.map(valueAsCql).toSet

  override def apply(r: Row): Set[RR] = {
    parse(r) match {
      case Success(set) => set

      // Note null sets are considered successful, we simply return an empty set instead of null.
      case Failure(ex) =>
        table.logger.error(ex.getMessage)
        throw ex
    }
  }

  override def asCql(v: Set[RR]): String = QueryBuilder.Collections.serialize(valuesToCType(v)).queryString
}


@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class SetColumn[
  Owner <: CassandraTable[Owner, Record],
  Record,
  RR : Primitive
](
  table: CassandraTable[Owner, Record]
) extends AbstractSetColumn[Owner, Record, RR](table) with PrimitiveCollectionValue[RR] {

  override val valuePrimitive: Primitive[RR] = Primitive[RR]

  val cassandraType: String = QueryBuilder.Collections.collectionType(
    CQLSyntax.Collections.set,
    valuePrimitive.cassandraType,
    shouldFreeze,
    valuePrimitive.frozen,
    isStaticColumn
  ).queryString

  override def valueAsCql(v: RR): String = Primitive[RR].asCql(v)

  override def fromString(c: String): RR = Primitive[RR].fromString(c)

  override def parse(r: Row): Try[Set[RR]] = {
    if (r.isNull(name)) {
      Success(Set.empty[RR])
    } else {
      Success(r.getSet(name, Primitive[RR].clz.asInstanceOf[Class[RR]]).asScala.toSet[RR])
    }
  }

}
