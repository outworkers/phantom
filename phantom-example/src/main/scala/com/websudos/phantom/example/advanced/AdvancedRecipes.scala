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
package com.websudos.phantom.example.advanced

import java.util.UUID
import scala.concurrent.{ Future => ScalaFuture }
import org.joda.time.DateTime

import com.datastax.driver.core.{ ResultSet, Row }

import com.websudos.phantom.Implicits._
import com.websudos.phantom.example.basics.{ExampleConnector, Recipe, Recipes}
import com.twitter.conversions.time._

/**
 * In this example we will create a  table storing recipes.
 * This time we will use a composite key formed by name and id.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
sealed class AdvancedRecipes private() extends CassandraTable[Recipes, Recipe] {
  // First the partition key, which is also a Primary key in Cassandra.
  object id extends  UUIDColumn(this) with PartitionKey[UUID] {
    // You can override the name of your key to whatever you like.
    // The default will be the name used for the object, in this case "id".
    override lazy  val name = "the_primary_key"
  }

  object name extends StringColumn(this)

  object title extends StringColumn(this)
  object author extends StringColumn(this)
  object description extends StringColumn(this)

  // Custom data types can be stored easily.
  // Cassandra collections target a small number of items, but usage is trivial.
  object ingredients extends SetColumn[Recipes, Recipe, String](this)
  object props extends MapColumn[Recipes, Recipe, String, String](this)
  object timestamp extends DateTimeColumn(this) with ClusteringOrder[DateTime]

  // Now the mapping function, transforming a row into a custom type.
  // This is a bit of boilerplate, but it's one time only and very short.
  def fromRow(row: Row): Recipe = {
    Recipe(
      id(row),
      name(row),
      title(row),
      author(row),
      description(row),
      ingredients(row),
      props(row),
      timestamp(row)
    )
  }
}


object AdvancedRecipes extends AdvancedRecipes with ExampleConnector {

  def insertRecipe(recipe: Recipe): ScalaFuture[ResultSet] = {
    insert.value(_.id, recipe.id)
      .value(_.author, recipe.author)
      .value(_.description, recipe.description)
      .value(_.ingredients, recipe.ingredients)
      .value(_.name, recipe.name)
      .value(_.props, recipe.props)
      .value(_.timestamp, recipe.timestamp)
      .ttl(150.minutes.inSeconds) // you can use TTL if you want to.
      .future()
  }

  // Like in the real world, you have now planned your queries ahead.
  // You know what you can do and what you can't based on the schema limitations.
  def getRecipeById(id: UUID): ScalaFuture[Option[Recipe]] = {
    select.where(_.id eqs id).one()
  }
}
