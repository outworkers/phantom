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
package com.outworkers.phantom.tables

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.dsl._
import org.json4s.Extraction
import org.json4s.native._

case class JsonTest(prop1: String, prop2: String)

case class JsonClass(
  id: UUID,
  name: String,
  json: JsonTest,
  jsonList: List[JsonTest],
  jsonSet: Set[JsonTest]
)


abstract class JsonTable extends CassandraTable[JsonTable, JsonClass] with RootConnector {

  implicit val formats = org.json4s.DefaultFormats

  object id extends UUIDColumn(this) with PartitionKey

  object name extends StringColumn(this)

  object json extends JsonColumn[JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = compactJson(renderJValue(Extraction.decompose(obj)))
  }

  object jsonList extends JsonListColumn[JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = {
      compactJson(renderJValue(Extraction.decompose(obj)))
    }
  }

  object jsonSet extends JsonSetColumn[JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = {
      compactJson(renderJValue(Extraction.decompose(obj)))
    }
  }

  def store(sample: JsonClass): InsertQuery.Default[JsonTable, JsonClass] = {
    insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
  }
}
