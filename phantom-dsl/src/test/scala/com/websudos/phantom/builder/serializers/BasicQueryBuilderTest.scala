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

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.query.{CQLQuery, QueryBuilderTest}

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
