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
import com.websudos.phantom.builder.query.QueryBuilderTest

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

    "should allow specifying WHERE options" - {
    }

    "should allow specifying CAS options" - {

      "should allow specifying a single IF clause" in {
        QueryBuilder.Update.onlyIf(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "IF a = b"
      }

      "should allow specifying an AND clause" in {
        QueryBuilder.Update.and(QueryBuilder.Where.eqs("a", "b")).queryString shouldEqual "AND a = b"
      }
    }

    "should allow specifying a ConsistencyLevel on an UPDATE clause" in {

    }
  }
}
