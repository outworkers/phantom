package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.tables.{Recipes, Recipe}
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.util.testing._
import com.websudos.phantom.dsl._

class PreparedSelectQueryTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Recipes.insertSchema()
  }
  
  it should "serialise and execute a prepared select statement with the correct number of arguments" in {
    val recipe = gen[Recipe]

    val operation = for {
      truncate <- Recipes.truncate.future
      insertDone <- Recipes.store(recipe).future()
      select <- Recipes.select.p_where(_.url eqs ?).bind(recipe.url).one()
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldEqual recipe
      }
    }
  }
}
