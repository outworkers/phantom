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

import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

import scala.concurrent.Future

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: List[String],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String],
  uid: UUID
)

abstract class Recipes extends Table[Recipes, Recipe] {

  object url extends StringColumn with PartitionKey

  object description extends OptionalStringColumn

  object ingredients extends ListColumn[String]

  object servings extends OptionalIntColumn

  object lastcheckedat extends DateTimeColumn

  object props extends MapColumn[String, String]

  object uid extends UUIDColumn
}

case class SampleEvent(id: UUID, map: Map[Long, DateTime])

abstract class Events extends Table[Events, SampleEvent] {
  object id extends UUIDColumn with PartitionKey
  object map extends MapColumn[Long, DateTime]

  def findById(id: UUID): Future[Option[SampleEvent]] = {
    select.where(_.id eqs id).one()
  }
}
