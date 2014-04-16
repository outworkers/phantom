package com.newzly.phantom.example.advanced

import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.ResultSet
import com.newzly.phantom.Implicits.context
import com.newzly.phantom.example.basics.Recipe

// In this section, we will show how you can create a real-world Cassandra service with phantom.
// First you have to think of what queries you need to perform. The usual.
// Say you come up with id and title.

// You will end up with several mapping tables enabling you to do all the queries you want.
// Now you are left with maintaining consistency at application level.
// We usually overlay a service on top of the mapping tables.
// To keep all the complexity away from other parts of the application.

object RecipesDatabaseService {

  // For instance, right now when you want to insert a new recipe.
  // Say from a JavaScript client with a fancy interface.
  // You need to insert one record into the actual table.
  // And another into the title -> id mapping table.

  // This is a trivial example showing how you can map and flatMap your path to glory.
  // Non blocking, 3 lines of code, 15 seconds of typing effort. Done.
  def insertRecipe(recipe: Recipe): ScalaFuture[ResultSet] = {
    AdvancedRecipes.insertRecipe(recipe) flatMap {
      _ => AdvancedRecipesByTitle.insertRecipe(recipe.title, recipe.id)
    }
  }
}
