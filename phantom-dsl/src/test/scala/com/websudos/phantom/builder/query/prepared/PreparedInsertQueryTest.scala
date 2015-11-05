package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.tables.{Recipe, Recipes}
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class PreparedInsertQueryTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Recipes.insertSchema()
  }

  it should "serialize an insert query" in {

    val sample = gen[Recipe]

    val query = Recipes.insert
      .p_value(_.uid, ?)
      .p_value(_.url, ?)
      .p_value(_.servings, ?)
      .p_value(_.ingredients, ?)
      .p_value(_.description, ?)
      .p_value(_.last_checked_at, ?)
      .p_value(_.props, ?)

  /*
    val exec = query.bind(
      sample.uid,
      sample.url,
      sample.servings,
      sample.ingredients,
      sample.description,
      sample.lastCheckedAt,
      sample.props
    ).future()

    val chain = for {
      store <- exec
      get <- Recipes.select.where(_.url eqs sample.url).one()
    } yield get

    whenReady(chain) {
      res => {
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  */
  }

}
