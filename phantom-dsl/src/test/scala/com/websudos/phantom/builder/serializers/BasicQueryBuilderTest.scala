package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{JsonUtils, CQLQuery, QueryBuilderTest}

class BasicQueryBuilderTest extends QueryBuilderTest {
  "The QueryBuilder should perform basic operations" - {
    "should append the JSON keyword to a insert statement" in {
      val qb = CQLQuery(s"INSERT INTO ${QueryBuilder.keyspace("phantom", "tableName").queryString}").append(QueryBuilder.json("""{"someJsonValue": 10}""", JsonUtils.formats)).queryString

      qb shouldEqual """INSERT INTO phantom."tableName" JSON '{"\"someJsonValue\"":10}'"""
    }

    "should append a keySpace string to a table name string with a dot separator" - {
      "should append the keyspace name if the table string name doesn't already contain it" in {
        val qb = QueryBuilder.keyspace("k", "t").queryString

        qb shouldEqual "k.t"
      }

      "should append the keyspace name if the table string name starts with the keyspace name" in {
        val qb = QueryBuilder.keyspace("recipes", "recipes_main").queryString
        qb shouldEqual "recipes.recipes_main"
      }

      "should not append the keyspace name if the table string already contains the keyspace definition" in {
        val qb = QueryBuilder.keyspace("recipes", "recipes.recipes_main").queryString

        qb shouldEqual "recipes.recipes_main"
      }
    }

    "should append a keyspace CQLQuery to a table name string with a dot separator" - {
      "should append the keyspace query if the table string name doesn't already contain it" in {
        val qb = QueryBuilder.keyspace("k", CQLQuery("t")).queryString

        qb shouldEqual "k.t"
      }

      "should append the keyspace query if the table string name starts with the keyspace name" in {
        val qb = QueryBuilder.keyspace("recipes", CQLQuery("recipes_main")).queryString
        qb shouldEqual "recipes.recipes_main"
      }

      "should not append the keyspace query if the table string already contains the keyspace definition" in {
        val qb = QueryBuilder.keyspace("recipes", CQLQuery("recipes.recipes_main")).queryString

        qb shouldEqual "recipes.recipes_main"
      }
    }
  }
}
