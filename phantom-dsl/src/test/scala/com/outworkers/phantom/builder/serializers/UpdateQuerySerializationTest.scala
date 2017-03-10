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

import com.outworkers.phantom.PhantomBaseSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone}

import scala.concurrent.duration._
import org.scalatest.FreeSpec

class UpdateQuerySerializationTest extends FreeSpec with PhantomBaseSuite with TestDatabase.connector.Connector {

  val comparisonValue = 5

  "An Update query should" - {

    "allow specifying USING clause options" - {

      "specify a consistency level of ALL in an AssignmentsQuery" in {

        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.protocolConsistency) {
          query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url';"
        } else {
          query shouldEqual s"UPDATE phantom.recipes USING CONSISTENCY ALL SET servings = 5 WHERE url = '$url';"
        }
      }

      "chain a ttl clause to an UpdateQuery" in {
        val url = gen[String]
        val uid = gen[UUID]

        val query = TestDatabase.recipes.update.where(_.url eqs url)
          .modify(_.uid setTo uid)
          .ttl(5.seconds)
          .queryString

        query shouldEqual s"UPDATE phantom.recipes USING TTL 5 SET uid = $uid WHERE url = '$url';"
      }

      "a timestamp setting on an assignments query" in {
        val url = gen[String]
        val uid = gen[UUID]
        val timestamp = gen[Long]

        val query = TestDatabase.recipes.update.where(_.url eqs url)
          .modify(_.uid setTo uid)
          .timestamp(timestamp)
          .queryString

        query shouldEqual s"UPDATE phantom.recipes USING TIMESTAMP $timestamp SET uid = $uid WHERE url = '$url';"
      }

      "a timestamp setting on an conditional assignments query specified before the onlyIf clause" in {
        val url = gen[String]
        val uid = gen[UUID]
        val timestamp = gen[Long]

        val query = TestDatabase.recipes.update.where(_.url eqs url)
          .modify(_.uid setTo uid)
          .timestamp(timestamp)
          .onlyIf(_.description is Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes USING TIMESTAMP $timestamp SET uid = $uid WHERE url = '$url' IF description = 'test';"
      }
    }

    "allow specifying conditional update clauses" - {

      "specify a consistency level in a ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description is Some("test"))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.protocolConsistency) {
          query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description = 'test';"
        } else {
          query shouldEqual s"UPDATE phantom.recipes USING CONSISTENCY ALL SET servings = 5 WHERE url = '$url' IF description = 'test';"
        }
      }

      "specify a non equals clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description isNot Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description != 'test';"
      }

      "specify a gt clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description isGt Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description > 'test';"
      }

      "specify a gte clause inside an ConditionalUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description isGte  Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description >= 'test';"
      }

      "specify a lt clause inside an ConditionalUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description isLt Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description < 'test';"
      }

      "specify a lte clause inside an ConditionalUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description isLte Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description <= 'test';"
      }

      "update a single entry inside a map column using a string apply" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update
          .where(_.url eqs url)
          .modify(_.props("test") setTo "test2")
          .queryString

        query shouldEqual s"UPDATE phantom.recipes SET props['test'] = 'test2' WHERE url = '$url';"
      }

      "update a single entry inside a map column using an int column" in {
        val id = gen[UUID]
        val dt = DateTime.now(DateTimeZone.UTC)
        val key = gen[Long]

        val query = TestDatabase.events.update
          .where(_.id eqs id)
          .modify(_.map(key) setTo dt)
          .queryString

        query shouldEqual s"UPDATE phantom.events SET map[$key] = ${dt.asCql()} WHERE id = $id;"
      }
    }
  }

}
