
/*
 * Copyright 2013 - 2018 Outworkers Ltd.
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

import java.util.UUID

import scala.concurrent.{Future => ScalaFuture}
import com.outworkers.phantom.dsl._
import org.joda.time.DateTime

case class NpeRecipe(
  id: UUID,
  name: String,
  title: String,
  author: String,
  description: String,
  ingredients: Set[String],
  props: Map[String, String],
  timestamp: DateTime
)


abstract class PrimitiveColumnRecipes extends Table[PrimitiveColumnRecipes, NpeRecipe] {
  object id extends Col[UUID] with PartitionKey
  object name extends Col[String]
  object title extends Col[String]
  object author extends Col[String]
  object description extends Col[String]
  object ingredients extends Col[Set[String]]

  object props extends Col[Map[String, String]]

  object timestamp extends Col[DateTime]

  def findRecipeById(id: UUID): ScalaFuture[Option[NpeRecipe]] = {
    select.where(_.id eqs id).one()
  }
}