package com.newzly.phantom.dsl.query

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.{Sampler, BaseTest}
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Articles, Recipes }
import com.datastax.driver.core.utils.UUIDs

class QuerySerializationTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace: String = "deleteTest"

  it should "correctly serialize a full select query" in {
    val someId = UUIDs.timeBased()
    Articles.select.where(_.id eqs someId).qb.toString shouldBe s"SELECT * FROM ${Articles.tableName} WHERE id=$someId;"
  }

  it should "correctly serialize a single column partial select query" in {
    val someId = UUIDs.timeBased()
    Articles.select(_.id).where(_.id eqs someId).qb.toString shouldBe s"SELECT ${Articles.id.name} FROM ${Articles.tableName} WHERE id=$someId;"
  }

  it should "correctly serialize a 2 column partial select query" in {
    val someId = UUIDs.timeBased()
    Articles.select(_.id, _.name).where(_.id eqs someId).qb.toString shouldBe s"SELECT ${Articles.id.name},${Articles.name.name} FROM ${Articles.tableName} WHERE id=$someId;"
  }

  ignore should "correctly serialize a 3 column partial select query" in {
    val someId = Sampler.getAUniqueString
    Recipes.select(
      _.url,
      _.description,
      _.ingredients
    ).where(_.url eqs someId).qb.toString shouldBe s"SELECT ${Recipes.url.name},${Recipes.description.name},${Recipes.ingredients.name} FROM ${Recipes.tableName} WHERE url=$someId;"
  }

}
