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

import com.websudos.phantom.builder.query.QueryBuilderTest
import com.websudos.phantom.tables.TestDatabase

class InsertQuerySerializationTest extends QueryBuilderTest {

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
        val query = Recipes.insert.json(compactRender(Extraction.decompose(sample))).queryString

        Console.println(query)
      }

      "should append USING clause after lightweight part " in {
        val smt = Recipes.insert.ifNotExists().value(_.url, "test").ttl(1000L)
        val query = Recipes.insert.ifNotExists().value(_.url, "test").ttl(1000L).queryString

        query shouldEqual "INSERT INTO phantom.Recipes (url) VALUES('test') IF NOT EXISTS USING TTL 1000;"
      }

}
  }

}
