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
import com.outworkers.phantom.builder.query.QueryBuilderTest
import com.outworkers.util.samplers._

class UpdateQueryBuilderTest extends QueryBuilderTest {

  "The UPDATE query builder" - {

    "should allow specifying SET options" - {

      "should allowing updating the value of a column with a setTo clause" in {
        QueryBuilder.Update.setTo("a", "b").queryString shouldEqual "a = b"
      }

      "should allow specifying a set clause in an UPDATE query" in {
        QueryBuilder.Update
          .set(QueryBuilder.Update.setTo("a", "b"))
          .queryString shouldEqual "SET a = b"
      }

      "should allow specifying multiple SET clauses and chaining them" in {
        val c1 = QueryBuilder.Update.setTo("a", "b")
        val c2 = QueryBuilder.Update.setTo("c", "d")

        QueryBuilder.Update.chain(List(c1, c2)).queryString shouldEqual "a = b, c = d"
      }
    }

    "should allow specifying USING clause options" - {
      "should allow specifying a timestamp clause" in {
        val str = gen[Long]
        QueryBuilder.timestamp(str).queryString shouldEqual s"TIMESTAMP $str"
      }
    }

    "should allow specifying CAS options" - {

      "should allow specifying a single IF clause" in {
        QueryBuilder.Update.onlyIf(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "IF a = b"
      }

      "should allow specifying an AND clause" in {
        QueryBuilder.Update.and(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "AND a = b"
      }
    }
  }
}
