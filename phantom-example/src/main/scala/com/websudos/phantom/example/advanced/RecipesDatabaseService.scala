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

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.ResultSet
import com.twitter.conversions.time._
import com.twitter.util.{Await, Future}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.example.basics.{ExampleConnector, Recipe}

// In this section, we will show how you can create a real-world Cassandra service with com.websudos.phantom.
// First you have to think of what queries you need to perform. The usual.
// Say you come up with id and author.

// You will end up with several mapping tables enabling you to do all the queries you want.
// Now you are left with maintaining consistency at application level.
// We usually overlay a service on top of the mapping tables.
// To keep all the complexity away from other parts of the application.

object RecipesDatabaseService extends ExampleConnector {

  /**
   * Right now you can go for a really neat trick of the trade.
   * You can automatically initialise all your tables using phnatom's schema auto-generation capabilities.
   * We are using the same connector as the tables do, which will link to the exact same database session.
   *
   * The bellow example uses the Future.join method which Twitter specific and not available in the less advanced Scala API.
   * Nonetheless, if you are using Scala you can almost replicate the below with a Future.sequence or Future.traverse over a List.
   *
   * This is a very neat and simple trick which will initialise all your tables in parallel at any time you want. The initialisation will automatically
   * trigger the mecbanism that connects to Cassandra and gives you back a session.
   */
  def init(): Unit = {
    val creation = Future.join(
      AdvancedRecipes.create.execute(),
      AdvancedRecipesByTitle.create.execute()
    )

    Await.ready(creation, 2.seconds)
  }


  // For instance, right now when you want to insert a new recipe.
  // Say from a JavaScript client with a fancy interface.
  // You need to insert one record into the actual table.
  // And another into the author -> id mapping table.

  // This is a trivial example showing how you can map and flatMap your path to glory.
  // Non blocking, 3 lines of code, 15 seconds of typing effort. Done.
  def insertRecipe(recipe: Recipe): ScalaFuture[ResultSet] = {
    for {
      insert <- AdvancedRecipes.insertRecipe(recipe)
      byTitle <- AdvancedRecipesByTitle.insertRecipe(Tuple2(recipe.title, recipe.id))
    } yield byTitle
  }
}
