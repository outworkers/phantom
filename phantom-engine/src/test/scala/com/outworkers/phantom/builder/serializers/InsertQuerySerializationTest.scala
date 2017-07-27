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

import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.phantom.tables.{Recipe, TestDatabase}
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._
import org.json4s.Extraction
import org.json4s.native._

class InsertQuerySerializationTest extends QueryBuilderTest {

  final val insertionTimeout = 1000L

  "An INSERT query" - {
    "should correctly chain the addition of columns and values to the builder" - {

      "should serialize the addition of a single value" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").queryString

        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test');"
      }

      "should serialize the addition of multiple values" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").value(_.ingredients, List("test")).queryString

        query shouldEqual "INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']);"
      }
    }

    "should allow specifying using clause options" - {

      "should allow specifying a TTL clause for an insert" in {
        val query = TestDatabase.recipes.insert
          .value(_.url, "test")
          .value(_.ingredients, List("test"))
          .ttl(5)
          .queryString

        query shouldEqual "INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']) USING TTL 5;"
      }

      "should allow specifying a timestamp clause" in {
        val time = new DateTime
        val query = TestDatabase.recipes.insert
          .value(_.url, "test")
          .value(_.ingredients, List("test"))
          .timestamp(time)
          .queryString

        query shouldEqual s"INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']) USING TIMESTAMP ${time.getMillis};"
      }

      "should allow specifying a combined TTL and timestamp clause" in {
        val time = new DateTime
        val ttl = 5

        val query = TestDatabase.recipes.insert
          .value(_.url, "test")
          .value(_.ingredients, List("test"))
          .timestamp(time)
          .ttl(ttl)
          .queryString

        query shouldEqual s"INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']) USING TIMESTAMP ${time.getMillis} AND TTL $ttl;"
      }
    }

    "should serialize lightweight transaction clauses irrespective of position in the DSL chain" - {

      "should append a lightweight clause to a single value query" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").ifNotExists().queryString

        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') IF NOT EXISTS;"
      }

      "should append a lightweight clause to a double value query" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").value(_.ingredients, List("test")).ifNotExists().queryString

        query shouldEqual "INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']) IF NOT EXISTS;"
      }

      "should append a lightweight clause to a single value query if used before the value set" in {
        val query = TestDatabase.recipes.insert.ifNotExists().value(_.url, "test").queryString

        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') IF NOT EXISTS;"
      }

      "should append a lightweight clause to a double value query if used before the value set" in {
        val query = TestDatabase.recipes.insert.ifNotExists().value(_.url, "test").value(_.ingredients, List("test")).queryString

        query shouldEqual "INSERT INTO phantom.recipes (url, ingredients) VALUES('test', ['test']) IF NOT EXISTS;"
      }

      "should serialize a JSON clause as the insert part" in {
        val sample = gen[Recipe]
        val json = compactJson(renderJValue(Extraction.decompose(sample)))
        val query = TestDatabase.recipes.insert.json(json).queryString

      }

      "should append USING clause after lightweight part " in {
        val query = TestDatabase.recipes.insert.ifNotExists().value(_.url, "test").ttl(insertionTimeout).queryString
        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') IF NOT EXISTS USING TTL 1000;"
      }

      "should allow specifying an IGNORE_NULLS clause as part of the using block" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").using(ignoreNulls).queryString
        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') USING IGNORE_NULLS;"
      }

      "should allow using operator values as parts of the insert statements" in {
        val query = TestDatabase.timeSeriesTable.insert
          .valueOp(_.id, now())
          .queryString

        query shouldEqual "INSERT INTO phantom.timeSeriesTable (id) VALUES(now());"
      }

    }
  }

}
