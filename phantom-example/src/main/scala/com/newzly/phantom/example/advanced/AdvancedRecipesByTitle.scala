package com.newzly.phantom.example.advanced

import java.util.UUID
import scala.concurrent.{ Future => ScalaFuture }
import com.datastax.driver.core.{ ResultSet, Row }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.example.basics.DBConnector


// Now you want to enable querying Recipes by title.
// Because of the massive performance overhead of filtering,
// you can't really use a SecondaryKey for multi-billion record databases.

// Instead, you create mapping tables and ensure consistency from the application level.
// This will illustrate just how easy it is to do that with phantom.
sealed class AdvancedRecipesByTitle extends CassandraTable[AdvancedRecipesByTitle, (String, UUID)] {

  // In this table, the title will be PrimaryKey and PartitionKey.
  object title extends StringColumn(this) with PartitionKey[String]

  // The id is just another normal field.
  object id extends UUIDColumn(this)

  def fromRow(row: Row): (String, UUID) = {
    Tuple2(title(row), id(row))
  }
}

object AdvancedRecipesByTitle extends AdvancedRecipesByTitle with DBConnector {
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
