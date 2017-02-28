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
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._

class DeleteQuerySerialisationTest extends QueryBuilderTest {

  val BasicTable = TestDatabase.basicTable

  "The DELETE query builder" - {
    "should generate table column deletion queries" - {
      "should create a delete query for a single column" - {
        val id = gen[UUID]
        val qb = BasicTable.delete.where(_.id eqs id).qb.queryString

        qb shouldEqual s"DELETE FROM $keySpace.${BasicTable.tableName} WHERE id = ${id.toString}"
      }

      "should create a conditional delete query if an onlyIf clause is used" in {
        val id = gen[UUID]

        val qb = BasicTable.delete.where(_.id eqs id)
          .onlyIf(_.placeholder is "test")
          .qb.queryString

        qb shouldEqual s"DELETE FROM $keySpace.${BasicTable.tableName} WHERE id = ${id.toString} IF placeholder = 'test'"
      }

      "should serialise a deleteColumn query, equivalent to an ALTER DROP" in {
        val id = gen[UUID]

        val qb = BasicTable.delete(_.placeholder).where(_.id eqs id)
          .qb.queryString

        qb shouldEqual s"DELETE placeholder FROM $keySpace.${BasicTable.tableName} WHERE id = ${id.toString}"
      }

      "should serialise a delete column query with a conditional clause" in {
        val id = gen[UUID]

        val qb = BasicTable.delete(_.placeholder).where(_.id eqs id)
          .onlyIf(_.placeholder is "test")
          .qb.queryString

        qb shouldEqual s"DELETE placeholder FROM $keySpace.${BasicTable.tableName} WHERE id = ${id.toString} IF placeholder = 'test'"
      }

      "should serialize a delete map column query" in {
        val url = gen[String]

        val qb = TestDatabase.recipes.delete(_.props("test")).where(_.url eqs url).queryString

        qb shouldEqual s"DELETE props['test'] FROM phantom.recipes WHERE url = '$url';"
      }

      "should serialize a delete query for multiple map properties" in {
        val url = gen[String]

        val qb = TestDatabase.recipes
          .delete(_.props("test"), _.props("test2"))
          .where(_.url eqs url).queryString

        qb shouldEqual s"DELETE props['test'], props['test2'] FROM phantom.recipes WHERE url = '$url';"
      }
    }

    "should allow specifying a lightweight clause" - {
      "should allow specifying an ifExists clause as part of a delete query" in {
        val url = gen[String]

        val qb = TestDatabase.recipes
          .delete.where(_.url eqs url)
          .ifExists
          .queryString

        qb shouldEqual s"DELETE FROM phantom.recipes WHERE url = '$url' IF EXISTS;"
      }

      "should allow specifying an ifExists clause and a conditional clause as part of a delete query" in {
        val url = gen[String]
        val value = gen[DateTime]

        val qb = TestDatabase.recipes
          .delete.where(_.url eqs url)
          .ifExists
          .onlyIf(_.lastcheckedat is value)
          .queryString

        qb shouldEqual s"DELETE FROM phantom.recipes WHERE url = '$url' IF EXISTS IF lastcheckedat = ${value.getMillis};"
      }
    }

    "should allow specifying a custom timestamp for deletes" - {
      "should allow using a milliseconds Long value as a timestamp" in {
        val value = gen[Long]
        val url = gen[String]

        val qb = TestDatabase.recipes
          .delete.where(_.url eqs url)
          .timestamp(value)
          .queryString

        qb shouldEqual s"DELETE FROM phantom.recipes USING TIMESTAMP $value WHERE url = '$url';"
      }

      "should allow using a DateTime instance value as a timestamp" in {
        val value = gen[DateTime]
        val url = gen[String]

        val qb = TestDatabase.recipes
          .delete.where(_.url eqs url)
          .timestamp(value)
          .queryString

        qb shouldEqual s"DELETE FROM phantom.recipes USING TIMESTAMP ${value.getMillis} WHERE url = '$url';"
      }

      "should allow mixing a timestamp clause with a conditional clause" in {
        val value = gen[DateTime]
        val url = gen[String]

        val qb = TestDatabase.recipes
          .delete.where(_.url eqs url)
          .timestamp(value)
          .onlyIf(_.lastcheckedat is value)
          .queryString

        qb shouldEqual s"DELETE FROM phantom.recipes USING TIMESTAMP ${value.getMillis} WHERE url = '$url' IF lastcheckedat = ${value.getMillis};"
      }
    }
  }
}
