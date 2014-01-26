package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import org.joda.time.DateTime


case class Author(
  firstName: String,
  lastName: String,
  bio: Option[String]
)

object Author extends ModelSampler[Author] {
  def sample: Author = {
    Author(
      Sampler.getAUniqueString,
      Sampler.getAUniqueString,
      Some(Sampler.getAUniqueString)
    )
  }
}

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: Seq[String],
  author: Option[Author],
  servings: Option[Int],
  lastCheckedAt: DateTime,
  props: Map[String, String]
)

object Recipe extends ModelSampler[Recipe] {
  def sample: Recipe = {
    Recipe(
      Sampler.getAUniqueString,
      Some(Sampler.getAUniqueString),
      Seq(Sampler.getAUniqueString, Sampler.getAUniqueString),
      Some(Author.sample),
      Some(Sampler.getARandomInteger()),
      new DateTime(),
      Map.empty[String, String]
    )
  }

  def samples(num: Int = 20): List[Recipe] = {
    List.range(1, num).map(x => { Recipe.sample })
  }
}

case class JsonSeqRow(pkey: String, jtsc: Seq[Recipe])

object JsonSeqRow extends ModelSampler[JsonSeqRow] {
  def sample: JsonSeqRow = {
    JsonSeqRow(
      Sampler.getAUniqueString,
      List.range(0, 30).map(x => {
        Recipe.sample
      }).toSeq
    )
  }
}

sealed class Recipes extends CassandraTable[Recipes, Recipe] {

  override def fromRow(r: Row): Recipe = {
    Recipe(
      url(r),
      description(r),
      ingredients(r),
      author.optional(r),
      servings(r),
      last_checked_at(r),
      props(r)
    )
  }

  val url = new StringColumn(this)

  val description = new OptionalStringColumn(this)

  val ingredients = new SeqColumn[Recipes, Recipe, String](this)

  val author = new JsonColumn[Recipes, Recipe, Author](this)

  val servings = new OptionalIntColumn(this)

  val last_checked_at = new DateTimeColumn(this)

  val props = new MapColumn[Recipes, Recipe, String, String](this)

  val uid = new UUIDColumn(this)
}


object Recipes extends Recipes with TestSampler[Recipes, Recipe] {
  override def tableName = "Recipes"

  def createSchema: String = {
    ""
  }
}