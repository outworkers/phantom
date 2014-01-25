package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }


case class Author(
  firstName: String,
  lastName: String,
  bio: Option[String]
)

object Author {
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

object Recipe extends ModelSampler {
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
}

case class JsonSeqColumnRow(pkey: String, jtsc: Seq[Recipe])

object JsonSeqColumnRow extends ModelSampler {
  def sample: JsonSeqColumnRow = {
    JsonSeqColumnRow(
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

  object url extends PrimitiveColumn[String]

  object description extends OptionalPrimitiveColumn[String]

  object ingredients extends SeqColumn[String]

  object author extends JsonColumn[Author]

  object servings extends OptionalPrimitiveColumn[Int]

  object last_checked_at extends PrimitiveColumn[Date]

  object props extends MapColumn[String, String]

  object uid extends PrimitiveColumn[UUID]

  val _key = url
}


object Recipes extends Recipes with TestSampler[Recipe] {
  override def tableName = "Recipes"

  def createSchema: String = {
    ""
  }
}