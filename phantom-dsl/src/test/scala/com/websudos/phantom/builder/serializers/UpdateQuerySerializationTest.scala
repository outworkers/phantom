package com.websudos.phantom.builder.serializers

import com.datastax.driver.core.ConsistencyLevel
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.websudos.phantom.testkit.suites.PhantomCassandraConnector
import com.websudos.util.testing._
import org.scalatest.{FreeSpec, Matchers}

class UpdateQuerySerializationTest extends FreeSpec with Matchers with PhantomCassandraConnector {

  "An Update query should" - {
    "allow specifying consistency levels" - {
      "specify a consistency level of ALL in an AssignmentsQuery" in {

        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(5))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.v3orNewer) {
          query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url'"
        } else {
          query shouldEqual s"UPDATE phantom.Recipes USING CONSISTENCY ALL SET servings = 5 WHERE url = '$url';"
        }
      }

      "specify a consistency level in a ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(5))
          .onlyIf(_.description is Some("test"))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.v3orNewer) {
          query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description = 'test'"
        } else {
          query shouldEqual s"UPDATE phantom.Recipes USING CONSISTENCY ALL SET servings = 5 WHERE url = '$url' IF description = 'test';"
        }
      }
    }
  }

}
