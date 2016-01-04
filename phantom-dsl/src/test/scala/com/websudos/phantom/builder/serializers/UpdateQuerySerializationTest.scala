/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.serializers

import com.websudos.phantom.PhantomBaseSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.websudos.util.testing._

import org.scalatest.FreeSpec

class UpdateQuerySerializationTest extends FreeSpec with PhantomBaseSuite {

  val comparisonValue = 10

  "An Update query should" - {
    "allow specifying consistency levels" - {
      "specify a consistency level of ALL in an AssignmentsQuery" in {

        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.v3orNewer) {
          query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url'"
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

        query shouldEqual s"UPDATE phantom.Recipes USING TTL 5 SET uid = $uid WHERE url = '$url';"
      }

      "specify a consistency level in a ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description is Some("test"))
          .consistencyLevel_=(ConsistencyLevel.ALL)
          .queryString

        if (session.v3orNewer) {
          query shouldEqual s"UPDATE phantom.recipes SET servings = 5 WHERE url = '$url' IF description = 'test'"
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

        query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description != 'test';"
      }

      "specify a gt clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description gt Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description > 'test';"
      }

      "specify a gte clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description gte Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description >= 'test';"
      }

      "specify a lt clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description lt Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description < 'test';"
      }

      "specify a lte clause inside an ConditionUpdateQuery" in {
        val url = gen[String]

        val query = TestDatabase.recipes.update()
          .where(_.url eqs url)
          .modify(_.servings setTo Some(comparisonValue))
          .onlyIf(_.description lte Some("test"))
          .queryString

        query shouldEqual s"UPDATE phantom.Recipes SET servings = 5 WHERE url = '$url' IF description <= 'test';"
      }

    }
  }

}
