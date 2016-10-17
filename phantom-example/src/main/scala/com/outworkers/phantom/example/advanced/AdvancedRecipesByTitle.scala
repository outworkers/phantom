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
package com.outworkers.phantom.example.advanced

import java.util.UUID

import com.datastax.driver.core.{ResultSet, Row}
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.dsl._

import scala.concurrent.{Future => ScalaFuture}


// Now you want to enable querying Recipes by author.
// Because of the massive performance overhead of filtering,
// you can't really use a SecondaryKey for multi-billion record databases.

// Instead, you create mapping tables and ensure consistency from the application level.
// This will illustrate just how easy it is to do that with com.outworkers.phantom.
sealed class AdvancedRecipesByTitle extends CassandraTable[ConcreteAdvancedRecipesByTitle, (String, UUID)] {

  // In this table, the author will be PrimaryKey and PartitionKey.
  object title extends StringColumn(this) with PartitionKey[String]

  // The id is just another normal field.
  object id extends UUIDColumn(this)

  def fromRow(row: Row): (String, UUID) = {
    Tuple2(title(row), id(row))
  }
}

abstract class ConcreteAdvancedRecipesByTitle extends AdvancedRecipesByTitle with RootConnector {
  override lazy val tableName = "recipes_by_title"

  def insertRecipe(recipe: (String, UUID)): ScalaFuture[ResultSet] = {
    insert.value(_.title, recipe._1).value(_.id, recipe._2).future()
  }

  // now you can have the tile in a where clause
  // without the performance impact of a secondary index.
  def findRecipeByTitle(title: String): ScalaFuture[Option[(String, UUID)]] = {
    select.where(_.title eqs title).one()
  }
}
