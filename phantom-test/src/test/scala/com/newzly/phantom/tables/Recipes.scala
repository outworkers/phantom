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
package com.newzly.phantom.tables

import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.util.testing.Sampler

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: List[String],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String]
)

object Recipe extends ModelSampler[Recipe] {
  def sample: Recipe = {
    Recipe(
      Sampler.getARandomString,
      Some(Sampler.getARandomString),
      List(Sampler.getARandomString, Sampler.getARandomString),
      Some(Sampler.getARandomInteger()),
      new DateTime(),
      Map.empty[String, String]
    )
  }

  def samples(num: Int = 20): List[Recipe] = {
    List.range(1, num).map(x => { Recipe.sample })
  }
}

sealed class Recipes extends CassandraTable[Recipes, Recipe] {

  override def fromRow(r: Row): Recipe = {
    Recipe(
      url(r),
      description(r),
      ingredients(r),
      servings(r),
      last_checked_at(r),
      props(r)
    )
  }

  object url extends StringColumn(this) with PartitionKey[String]

  object description extends OptionalStringColumn(this)

  object ingredients extends ListColumn[Recipes, Recipe, String](this)

  object servings extends OptionalIntColumn(this)

  object last_checked_at extends DateTimeColumn(this)

  object props extends MapColumn[Recipes, Recipe, String, String](this)

  object uid extends UUIDColumn(this)
}

object Recipes extends Recipes with TestSampler[Recipes, Recipe] {
  override def tableName = "Recipes"

}
