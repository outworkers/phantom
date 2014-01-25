package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{ Sampler, TestSampler }


case class Author(
  firstName: String,
  lastName: String,
  bio: Option[String]
)

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: Seq[String],
  author: Option[Author],
  servings: Option[Int],
  lastCheckedAt: java.util.Date,
  props: Map[String, String]
)

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
  def sample: Recipe = {
    Recipe(
      Sampler.getAUniqueString,
      Some(Sampler.getAUniqueString),
      Seq(Sampler.getAUniqueString, Sampler.getAUniqueString),
      None,
      Some(Sampler.getARandomInteger()),
      new Date(),
      Map.empty[String, String]
    )
  }
}