/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.example.advanced

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row}
import com.twitter.conversions.time._
import com.websudos.phantom.dsl._
import com.websudos.phantom.example.basics.Recipe
import org.joda.time.DateTime

import scala.concurrent.{Future => ScalaFuture}

/**
 * In this example we will create a  table storing recipes.
 * This time we will use a composite key formed by name and id.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
sealed class AdvancedRecipes extends CassandraTable[ConcreteAdvancedRecipes, Recipe] {
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
  object ingredients extends SetColumn[String](this)
  object props extends MapColumn[String, String](this)
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


abstract class ConcreteAdvancedRecipes extends AdvancedRecipes with RootConnector {

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
  def findById(id: UUID): ScalaFuture[Option[Recipe]] = {
    select.where(_.id eqs id).one()
  }
}
