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

import scala.annotation.implicitNotFound
import com.websudos.phantom.{ CassandraPrimitive, CassandraTable }

@implicitNotFound(msg = "Type ${K} and ${V} must be Cassandra primitives")
class MapColumn[Owner <: CassandraTable[Owner, Record], Record, K: CassandraPrimitive, V: CassandraPrimitive](table: CassandraTable[Owner, Record])
    extends AbstractMapColumn[Owner, Record, K, V](table) with PrimitiveCollectionValue[V] {

  val keyPrimitive = CassandraPrimitive[K]

  override def keyCls: Class[_] = keyPrimitive.cls

  override def keyToCType(v: K): AnyRef = keyPrimitive.toCType(v)

  override def keyFromCType(c: AnyRef): K = keyPrimitive.fromCType(c)

  override val valuePrimitive = CassandraPrimitive[V]

  override val cassandraType = s"map<${keyPrimitive.cassandraType}, ${valuePrimitive.cassandraType}>"
}
