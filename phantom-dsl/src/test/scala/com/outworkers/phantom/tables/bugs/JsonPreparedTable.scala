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
package com.outworkers.phantom.tables.bugs

import com.outworkers.phantom.ResultSet
import com.outworkers.phantom.dsl._
import org.json4s.Extraction
import org.json4s.native._

import scala.concurrent.Future

case class JsonUser(
  id: Int,
  name: String
)

object JsonUser {
  implicit val formats = org.json4s.DefaultFormats

  implicit val jsonPrimitive: Primitive[JsonUser] = {
    Primitive.json[JsonUser](js => compactJson(renderJValue(Extraction.decompose(js))))(JsonParser.parse(_).extract[JsonUser])
  }
}

case class NestedJsonRecord(
  id: Int,
  name: String,
  description: Option[String],
  user: JsonUser
)

abstract class JsonPreparedTable extends Table[JsonPreparedTable, NestedJsonRecord] {

  object id extends IntColumn with PartitionKey
  object name extends StringColumn
  object description extends OptionalStringColumn

  object user extends JsonColumn[JsonUser]

  lazy val preparedInsert = insert
    .p_value(_.id, ?)
    .p_value(_.name, ?)
    .p_value(_.description, ?)
    .p_value(_.user, ?)
    .prepare()

  def insertItem(item: NestedJsonRecord): Future[ResultSet] =
    preparedInsert.bind(item).future()

  def findById(id: Int): Future[Option[NestedJsonRecord]] = {
    select.where(_.id eqs id).one()
  }
}
