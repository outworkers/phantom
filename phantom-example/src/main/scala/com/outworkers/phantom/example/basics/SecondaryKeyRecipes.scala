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
package com.outworkers.phantom.example.basics

import java.util.UUID
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.Row
import com.websudos.phantom.dsl._

/**
 * In this example we will create a table storing recipes with a SecondaryKey.
 * This time we will use a non-composite Primary key with a SecondaryKey on author.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
sealed class SecondaryKeyRecipes extends CassandraTable[ConcreteSecondaryKeyRecipes, Recipe] {
  // First the partition key, which is also a Primary key in Cassandra.
  object id extends  UUIDColumn(this) with PartitionKey[UUID] {
    // You can override the name of your key to whatever you like.
    // The default will be the name used for the object, in this case "id".
    override lazy  val name = "the_primary_key"
  }

  object name extends StringColumn(this) with PrimaryKey[String]

  object title extends StringColumn(this)

  // If you want to query by a field, you need an index on it.
  // One of the strategies for doing so is using a SecondaryKey
  object author extends StringColumn(this) with Index[String] // done

  object description extends StringColumn(this)

  object ingredients extends SetColumn[String](this)
  object props extends MapColumn[String, String](this)
  object timestamp extends DateTimeColumn(this)

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


abstract class ConcreteSecondaryKeyRecipes extends SecondaryKeyRecipes with RootConnector {

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
