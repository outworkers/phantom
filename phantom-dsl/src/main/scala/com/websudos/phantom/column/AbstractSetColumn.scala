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

import java.util.{ Set => JavaSet }
import scala.collection.JavaConverters._
import scala.util.Try
import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable

abstract class AbstractSetColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, Set[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): JavaSet[AnyRef] =
    values.map(valueToCType).toSet.asJava

  override def toCType(values: Set[RR]): AnyRef = valuesToCType(values)

  override def apply(r: Row): Set[RR] = {
    optional(r).getOrElse(Set.empty[RR])
  }

  override def optional(r: Row): Option[Set[RR]] = {
    Try {
      r.getSet(name, valueCls).asScala.map(e => valueFromCType(e.asInstanceOf[AnyRef])).toSet
    }.toOption
  }
}
