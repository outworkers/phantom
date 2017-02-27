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
package com.outworkers.phantom.example.basics

import java.util.UUID

import scala.concurrent.{Future => ScalaFuture}
import com.datastax.driver.core.Row
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

/**
 * In this example we will create a table storing recipes with a SecondaryKey.
 * This time we will use a non-composite Primary key with a SecondaryKey on author.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
abstract class SecondaryKeyRecipes extends CassandraTable[SecondaryKeyRecipes, Recipe] with RootConnector {
  // First the partition key, which is also a Primary key in Cassandra.
  object id extends  UUIDColumn(this) with PartitionKey {
    // You can override the name of your key to whatever you like.
    // The default will be the name used for the object, in this case "id".
    override lazy  val name = "the_primary_key"
  }

  object name extends StringColumn(this) with PrimaryKey

  object title extends StringColumn(this)

  // If you want to query by a field, you need an index on it.
  // One of the strategies for doing so is using a SecondaryKey
  object author extends StringColumn(this) with Index // done

  object description extends StringColumn(this)

  object ingredients extends SetColumn[String](this)
  object props extends MapColumn[String, String](this)
  object timestamp extends DateTimeColumn(this)

  // Now say you want to get a Recipe by author.
  // author is a Index, you can now use it in a "where" clause.
  // Performance is unpredictable for such queries, so you need to allow filtering.
  // Note this is not the best practice.
  // In a real world environment, you create a RecipesByTitle mapping table.
  // Check out the example.
  def findRecipeByAuthor(author: String): ScalaFuture[Option[Recipe]] = {
    select.allowFiltering().where(_.author eqs author).one()
  }
}
