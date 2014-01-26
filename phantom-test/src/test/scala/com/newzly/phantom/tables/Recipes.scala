package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._


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
  lastCheckedAt: java.util.Date,
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
      new Date(),
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
    Recipe(url(r), description(r), ingredients(r), author.optional(r), servings(r), last_checked_at(r), props(r))
  }

  object url extends PrimitiveColumn[Recipes, Recipe, String](this)

  object description extends OptionalPrimitiveColumn[Recipes, Recipe, String](this)

  object ingredients extends SeqColumn[Recipes, Recipe, String](this)

  object author extends JsonColumn[Recipes, Recipe, Author](this)

  object servings extends OptionalPrimitiveColumn[Recipes, Recipe, Int](this)

  object last_checked_at extends PrimitiveColumn[Recipes, Recipe, Date](this)

  object props extends MapColumn[Recipes, Recipe, String, String](this)

  object uid extends PrimitiveColumn[Recipes, Recipe, UUID](this)

  val _key = url
}


object Recipes extends Recipes with TestSampler[Recipes, Recipe] {
  override def tableName = "Recipes"

  def createSchema: String = {
    ""
  }
}