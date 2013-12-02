package com.newzly.phantom.dsl.test

import com.newzly.cassandra.phantom.{CassandraTable}
import com.newzly.cassandra.phantom.Implicits._
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import com.datastax.driver.core.{ Session, Row }
import scala.concurrent.{ Await, Future }
import java.util.UUID
import com.datastax.driver.core.utils.UUIDs

case class Author(firstName: String, lastName: String, bio: Option[String])

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: Seq[String],
  author: Option[Author],
  servings: Option[Int],
  lastCheckedAt: java.util.Date,
  props: Map[String, String])

class Recipes extends CassandraTable[Recipes, Recipe]("recipes") {


  override def fromRow(r: Row): Recipe = {
    Recipe(url(r), description(r), ingredients(r), author.optional(r), servings(r), lastCheckedAt(r), props(r))
  }

  val url = column[String]("url")
  val description = optColumn[String]("description")
  val ingredients = seqColumn[String]("ingredients")
  val author = jsonColumn[Author]("author")
  val servings = optColumn[Int]("servings")
  val lastCheckedAt = column[java.util.Date]("last_checked_at")
  val props = mapColumn[String, String]("props")
  val uid = column[UUID]("uid")
}

object Recipes extends Recipes

class PhantomSpec extends CassandraSpec {

  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, Duration(5, "seconds"))
    }
  }

  implicit val _session: Session = session

  "Blackpepper DSL" should {

    val author = Author("Tony", "Clark", Some("great chef..."))
    val r = Recipe("recipe_url", Some("desc"), Seq("ingr1", "ingr2"), Some(author), Some(4), new java.util.Date, Map("a" -> "b", "c" -> "d"))

    "support inserting, updating and deleting rows" in {
      Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.author, r.author)
        .valueOrNull(_.servings, r.servings)
        .value(_.lastCheckedAt, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased())
        .execute().sync()

      val recipeF: Future[Option[Recipe]] = Recipes.select.one
      recipeF.sync() should beSome(r)

      Recipes.select.fetch.sync() should contain(r)

      Recipes.update.where(_.url eqs r.url).modify(_.description setTo Some("new desc")).execute().sync()

      Recipes.select(_.description).where(_.url eqs r.url).one.map(_.flatten).sync() should beSome("new desc")

      Recipes.delete.where(_.url eqs r.url).execute().sync()
      Recipes.select.fetch.sync() should beEmpty
    }

  }
}
