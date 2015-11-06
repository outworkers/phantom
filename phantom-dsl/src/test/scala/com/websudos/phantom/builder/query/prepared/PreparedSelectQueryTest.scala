package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.util.testing._


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
      select <- Recipes.select.p_where(_.url eqs ?).bind(recipe.url.tp).one()
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value shouldEqual recipe
      }
    }
  }

  it should "serialzie and execute a prepared statement with 2 arguments" in {
    val sample = gen[Article]
    val owner = gen[UUID]
    val category = gen[UUID]

    val op = for {
      store <- ArticlesByAuthor.store(owner, category, sample).future()
      get <- ArticlesByAuthor.select.p_where(_.author_id eqs ?).p_and(_.category eqs ?).bind(owner, category).one()
    } yield get

    whenReady(op) {
      res => {
        res shouldBe defined
        res.value shouldEqual sample
      }
    }
  }
}
