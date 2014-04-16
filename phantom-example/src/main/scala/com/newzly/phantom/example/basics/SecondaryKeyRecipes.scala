package com.newzly.phantom.example.basics

import java.util.UUID
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.Row
import com.newzly.phantom.Implicits._

/**
 * In this example we will create a table storing recipes with a SecondaryKey.
 * This time we will use a non-composite Primary key with a SecondaryKey on author.
 */

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
sealed class SecondaryKeyRecipes extends CassandraTable[Recipes, Recipe] {
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
  object author extends StringColumn(this) with SecondaryKey[String] // done

  object description extends StringColumn(this)

  object ingredients extends SetColumn[Recipes, Recipe, String](this)
  object props extends MapColumn[Recipes, Recipe, String, String](this)
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


object SecondaryKeyRecipes extends SecondaryKeyRecipes with DBConnector {

  // Now say you want to get a Recipe by title.
  // title is a SecondaryKey, you can now use it in a "where" clause.
  // Performance is unpredicable for such queries, so you need to allow filtering.
  // Note this is not the best practice.
  // In a real world environment, you create a RecipesByTitle mapping table.
  // Check out the example.
  def getRecipeByTitle(title: String): ScalaFuture[Option[Recipe]] = {
    select.allowFiltering().where(_.title eqs title).one()
  }



}
