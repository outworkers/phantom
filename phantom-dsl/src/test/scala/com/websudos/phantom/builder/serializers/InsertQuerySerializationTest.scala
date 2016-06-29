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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.{Recipe, TestDatabase}
import com.outworkers.util.testing._
import com.websudos.phantom.dsl._
import net.liftweb.json.{ compactRender, Extraction }

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
        val query = TestDatabase.recipes.insert.json(compactRender(Extraction.decompose(sample))).queryString

      }

      "should append USING clause after lightweight part " in {
        val query = TestDatabase.recipes.insert.ifNotExists().value(_.url, "test").ttl(insertionTimeout).queryString
        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') IF NOT EXISTS USING TTL 1000;"
      }

      "should allow specifying an IGNORE_NULLS clause as part of the using block" in {
        val query = TestDatabase.recipes.insert.value(_.url, "test").using(ignoreNulls).queryString
        query shouldEqual "INSERT INTO phantom.recipes (url) VALUES('test') USING IGNORE_NULLS;"
      }

    }
  }

}
