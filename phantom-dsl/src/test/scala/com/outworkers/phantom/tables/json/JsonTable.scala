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
package com.outworkers.phantom.tables.json

import com.outworkers.phantom.dsl._

import io.circe._
import io.circe.generic.semiauto._
import io.circe.parser._
import io.circe.syntax._

case class JsonRecord(
  prop1: String,
  prop2: String
)

object JsonRecord {

  implicit val jsonDecoder: Decoder[JsonRecord] = deriveDecoder[JsonRecord]
  implicit val jsonEncoder: Encoder[JsonRecord] = deriveEncoder[JsonRecord]

  implicit val jsonPrimitive: Primitive[JsonRecord] = {
    Primitive.json[JsonRecord](_.asJson.noSpaces)(decode[JsonRecord](_).right.get)
  }
}

case class JsonClass(
  id: UUID,
  name: String,
  json: JsonRecord,
  optionalJson : Option[JsonRecord],
  jsonList: List[JsonRecord],
  jsonSet: Set[JsonRecord]
)

abstract class JsonTable extends Table[JsonTable, JsonClass] {

  object id extends UUIDColumn with PartitionKey

  object name extends StringColumn

  object json extends JsonColumn[JsonRecord]

  object optionalJson extends OptionalJsonColumn[JsonRecord]

  object jsonList extends JsonListColumn[JsonRecord]

  object jsonSet extends JsonSetColumn[JsonRecord]

}