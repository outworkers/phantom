/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.phantom.builder.query.QueryBuilderTest

class BasicQueryBuilderTest extends QueryBuilderTest {
  "The QueryBuilder should perform basic operations" - {

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
