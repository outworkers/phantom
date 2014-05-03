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

import scala.annotation.implicitNotFound
import scala.collection.JavaConverters._
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }
import com.twitter.util.Try

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class ListColumn[Owner <: CassandraTable[Owner, Record], Record, RR: CassandraPrimitive](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, List[RR]](table) {
  val primitive = CassandraPrimitive[RR]
  val cassandraType = s"list<${primitive.cassandraType}>"

  def toCType(values: List[RR]): AnyRef = values.map(CassandraPrimitive[RR].toCType).asJava

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(List.empty[RR])
  }

  def optional(r: Row): Option[List[RR]] = {
    Try {
      r.getList(name, primitive.cls).asScala.map {
        el => primitive.fromCType(el.asInstanceOf[AnyRef])
      }.toList
    }.toOption
  }
}
