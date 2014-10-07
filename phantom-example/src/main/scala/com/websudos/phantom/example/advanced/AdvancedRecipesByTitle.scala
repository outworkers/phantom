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
import com.datastax.driver.core.{ ResultSet, Row }
import com.websudos.phantom.Implicits._
import com.websudos.phantom.example.basics.ExampleConnector


// Now you want to enable querying Recipes by author.
// Because of the massive performance overhead of filtering,
// you can't really use a SecondaryKey for multi-billion record databases.

// Instead, you create mapping tables and ensure consistency from the application level.
// This will illustrate just how easy it is to do that with com.websudos.phantom.
sealed class AdvancedRecipesByTitle extends CassandraTable[AdvancedRecipesByTitle, (String, UUID)] {

  // In this table, the author will be PrimaryKey and PartitionKey.
  object title extends StringColumn(this) with PartitionKey[String]

  // The id is just another normal field.
  object id extends UUIDColumn(this)

  def fromRow(row: Row): (String, UUID) = {
    Tuple2(title(row), id(row))
  }
}

object AdvancedRecipesByTitle extends AdvancedRecipesByTitle with ExampleConnector {
  override lazy val tableName = "recipes_by_title"


  def insertRecipe(recipe: (String, UUID)): ScalaFuture[ResultSet] = {
    insert.value(_.title, recipe._1).value(_.id, recipe._2).future()
  }

  // now you can have the tile in a where clause
  // without the performance impact of a secondary index.
  def getRecipeByTitle(title: String): ScalaFuture[Option[(String, UUID)]] = {
    select.where(_.title eqs title).one()
  }
}
