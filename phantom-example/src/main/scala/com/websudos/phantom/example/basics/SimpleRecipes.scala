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
package com.websudos.phantom.example.basics

import java.util.UUID
import scala.concurrent.{ Future => ScalaFuture }

import org.joda.time.DateTime

import com.datastax.driver.core.{ ResultSet, Row }

import com.websudos.phantom.Implicits._
import com.websudos.phantom.iteratee.Iteratee

import com.twitter.conversions.time._

/**
 * In this example we will create a simple table storing recipes.
 * Data modeling with com.websudos.phantom is trivial and covers some of the more advanced features of Cassandra.
 *
 * Phantom will auto-generate the CQL3 table definition from your Scala code.
 * And you can automatically insert the schema during tests or even live environments.
 *
 * This is a very straightforward example, with a non composite Partition key
 */
case class Recipe(
 id: UUID,
 name: String,
 title: String,
 author: String,
 description: String,
 ingredients: Set[String],
 props: Map[String, String],
 timestamp: DateTime
)

// You can seal the class and only allow importing the companion object.
// The companion object is where you would implement your custom methods.
// Keep reading for examples.
sealed class Recipes extends CassandraTable[Recipes, Recipe] {
  object id extends  UUIDColumn(this) with PartitionKey[UUID] {
    // You can override the name of your key to whatever you like.
    // The default will be the name used for the object, in this case "id".
    override lazy val name = "the_primary_key"
  }

  // Now we define a column for each field in our case class.
  object name extends StringColumn(this)
  object title extends StringColumn(this)
  object author extends StringColumn(this)
  object description extends StringColumn(this)

  // Custom data types can be stored easily.
  // Cassandra collections target a small number of items, but usage is trivial.
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


object Recipes extends Recipes with ExampleConnector {
  // you can even rename the table in the schema to whatever you like.
  override lazy val tableName = "my_custom_table"

  // Inserting has a bit of boilerplate on its on.
  // But it's almost always a once per table thing, hopefully bearable.
  // Whatever values you leave out will be inserted as nulls into Cassandra.
  def insertNewRecord(recipe: Recipe): ScalaFuture[ResultSet] = {
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

  // now you have the full power of Cassandra in really cool one liners.
  // The future will do all the heavy lifting for you.
  // If there is an error you get a failed Future.
  // If there is no matching record you get a None.
  // The "one" method will select a single record, as it's name says.
  // It will always have a LIMIT 1 in the query sent to Cassandra.
  // select.where(_.id eqs UUID.randomUUID()).one() translates to
  // SELECT * FROM my_custom_table WHERE id = the_id_value LIMIT 1;
  def getRecipeById(id: UUID): ScalaFuture[Option[Recipe]] = {
    select.where(_.id eqs id).one()
  }

  // com.websudos.phantom allows partial selects from any query.
  // this is currently limited to 22 fields.
  def getRecipeIngredients(id: UUID): ScalaFuture[Option[Set[String]]] = {
    select(_.ingredients).where(_.id eqs id).one()
  }

  // Because you are using a partition key, you can successfully using ordering
  // And you can paginate your records.
  // That's it, a really cool one liner.
  // The fetch method will collect an asynchronous lazy iterator into a Seq.
  // It's a good way to avoid boilerplate when retrieving a small number of items.
  def getRecipesPage(start: UUID, limit: Int): ScalaFuture[Seq[Recipe]] = {
    select.where(_.id gtToken start).limit(limit).fetch()
  }


  // The fetchEnumerator method is the real power behind the scenes.
  // You can retrieve a whole table, even with billions of records, in a single query.
  // Phantom will collect them into an asynchronous, lazy iterator with very low memory foot print.
  // Enumerators, iterators and iteratees are based on Play iteratees.
  // You can keep the async behaviour or collect through the Iteratee.
  def getEntireTable: ScalaFuture[Seq[Recipe]] = {
    select.fetchEnumerator() run Iteratee.collect()
  }


  // com.websudos.phantom supports a few more Iteratee methods.
  // However, if you are looking to guarantee ordering and paginate "the old way"
  // You need an OrderPreservingPartitioner.
  def getRecipePage(start: Int, limit: Int): ScalaFuture[Iterator[Recipe]] = {
    select.fetchEnumerator() run Iteratee.slice(start, limit)
  }


  // Updating records is also really easy.
  // Updating one record is done like this
  def updateRecipeAuthor(id: UUID, author: String): ScalaFuture[ResultSet] = {
    update.where(_.id eqs id).modify(_.author setTo author).future()
  }

  // Updating records is also really easy.
  // Updating multiple fields at the same time is also easy.
  def updateRecipeAuthorAndName(id: UUID, name: String, author: String): ScalaFuture[ResultSet] = {
    update.where(_.id eqs id).modify(_.name setTo name)
      .and(_.author setTo author)
      .future()
  }

  // Deleting records has the same restrictions and selecting.
  // If something is non primary, you cannot have it in a where clause.
  def deleteRecipeById(id: UUID): ScalaFuture[ResultSet] = {
    delete.where(_.id eqs id).future()
  }
}
