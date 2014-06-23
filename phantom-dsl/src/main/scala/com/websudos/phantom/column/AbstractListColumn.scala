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

import java.util.{ List => JavaList }
import scala.collection.JavaConverters._
import scala.util.Try
import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable

abstract class AbstractListColumn[Owner <: CassandraTable[Owner, Record], Record, RR](table: CassandraTable[Owner, Record])
    extends Column[Owner, Record, List[RR]](table) with CollectionValueDefinition[RR] {

  def valuesToCType(values: Iterable[RR]): JavaList[AnyRef] =
    values.map(valueToCType).toList.asJava

  override def toCType(values: List[RR]): AnyRef = valuesToCType(values)

  override def apply(r: Row): List[RR] = {
    optional(r).getOrElse(Nil)
  }

  override def optional(r: Row): Option[List[RR]] = {
    Try {
      r.getList(name, valueCls).asScala.map(e => valueFromCType(e.asInstanceOf[AnyRef])).toList
    }.toOption
  }
}
