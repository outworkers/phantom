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

import com.outworkers.phantom.dsl._
import scala.concurrent.{Future => ScalaFuture}

/**
 * In this example we will create a  table storing recipes.
 * This time we will use a composite key formed by name and id.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
abstract class CompositeKeyRecipes extends Table[CompositeKeyRecipes, Recipe] {
  // First the partition key, which is also a Primary key in Cassandra.
  object id extends UUIDColumn with PartitionKey {
    // You can override the name of your key to whatever you like.
    // The default will be the name used for the object, in this case "id".
    override lazy  val name = "the_primary_key"
  }

  // Now we define a column for each field in our case class.
  // If we want to add another key to our composite, simply mixin PrimaryKey[ValueType]
  object name extends StringColumn with PrimaryKey // and you're done


  object title extends StringColumn
  object author extends StringColumn
  object description extends StringColumn

  // Custom data types can be stored easily.
  // Cassandra collections target a small number of items, but usage is trivial.
  object ingredients extends SetColumn[String]
  object props extends MapColumn[String, String]
  object timestamp extends DateTimeColumn

  // now you can use composite keys in the normal way.
  // If you would select only by id,
  // Cassandra will tell you a part of the primary is missing from the where clause.
  // Querying by composite keys is trivial using the "and" operator.
  def findRecipeByIdAndName(id: UUID, name: String): ScalaFuture[Option[Recipe]] = {
    select.where(_.id eqs id).and(_.name eqs name).one()
  }
}
