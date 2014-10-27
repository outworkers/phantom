/*
 *
 *  * Copyright 2014 newzly ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */


package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.datastax.driver.core.utils.UUIDs
import com.websudos.util.testing.Sampler
import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector
import com.websudos.phantom.helper.{ModelSampler, TestSampler}
import net.liftweb.json.{DefaultFormats, Extraction, JsonParser, pretty, render}


case class JsonTest(prop1: String, prop2: String)

object JsonTest extends ModelSampler[JsonTest] {
  def sample: JsonTest = JsonTest(
    Sampler.getARandomString,
    Sampler.getARandomString
  )
}

case class JsonClass(
  id: UUID,
  name: String,
  json: JsonTest,
  jsonList: List[JsonTest],
  jsonSet: Set[JsonTest]
)


class JsonTable extends CassandraTable[JsonTable, JsonClass] {

  implicit val formats = DefaultFormats

  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object name extends StringColumn(this)

  object json extends JsonColumn[JsonTable, JsonClass, JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = {
      pretty(render(Extraction.decompose(obj)))
    }
  }

  object jsonList extends JsonListColumn[JsonTable, JsonClass, JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = {
      pretty(render(Extraction.decompose(obj)))
    }
  }

  object jsonSet extends JsonSetColumn[JsonTable, JsonClass, JsonTest](this) {
    override def fromJson(obj: String): JsonTest = {
      JsonParser.parse(obj).extract[JsonTest]
    }

    override def toJson(obj: JsonTest): String = {
      pretty(render(Extraction.decompose(obj)))
    }
  }

  def fromRow(row: Row): JsonClass = {
    JsonClass(
      id(row),
      name(row),
      json(row),
      jsonList(row),
      jsonSet(row)
    )
  }
}

object JsonTable extends JsonTable with TestSampler[JsonTable, JsonClass] with PhantomCassandraConnector {

  def sample: JsonClass = {
    JsonClass(
      UUIDs.timeBased(),
      Sampler.getARandomString,
      JsonTest.sample,
      Iterator.fill(10)(JsonTest.sample).toList,
      Iterator.fill(10)(JsonTest.sample).toSet[JsonTest]
    )
  }
}
