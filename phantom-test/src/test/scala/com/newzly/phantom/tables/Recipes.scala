package com.newzly.phantom.tables

import java.util.{ Date, UUID }
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import org.joda.time.DateTime
import com.newzly.phantom.keys.{PartitionKey, PrimaryKey}

case class Recipe(
  url: String,
  description: Option[String],
  ingredients: Seq[String],
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
      Some(Sampler.getARandomInteger()),
      new DateTime(),
      Map.empty[String, String]
    )
  }

  def samples(num: Int = 20): List[Recipe] = {
    List.range(1, num).map(x => { Recipe.sample })
  }
}

sealed class Recipes extends CassandraTable[Recipes, Recipe] {

  override def fromRow(r: Row): Recipe = {
    Recipe(
      url(r),
      description(r),
      ingredients(r),
      servings(r),
      last_checked_at(r),
      props(r)
    )
  }

  def meta = Recipes

  object url extends StringColumn(this) with PartitionKey[String]

  object description extends OptionalStringColumn(this)

  object ingredients extends SeqColumn[Recipes, Recipe, String](this)

  object servings extends OptionalIntColumn(this)

  object last_checked_at extends DateTimeColumn(this)

  object props extends MapColumn[Recipes, Recipe, String, String](this)

  object uid extends UUIDColumn(this)
}

object Recipes extends Recipes with TestSampler[Recipes, Recipe] {
  override def tableName = "Recipes"

}