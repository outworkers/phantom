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
package com.outworkers.phantom.builder.query.engine

import com.outworkers.phantom.builder.syntax.CQLSyntax
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}


class CQLQueryTest extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = PropertyCheckConfiguration(minSuccessful = 100)

  it should "create an empty CQL query using the empty method on the companion object" in {
    CQLQuery.empty.queryString shouldEqual ""
  }

  it should "correctly identify if a CQL query is empty" in {
    forAll {(q1: String) =>
      whenever(q1.nonEmpty) {
        CQLQuery(q1).nonEmpty shouldEqual true
      }
    }
  }

  it should "append one query to another using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).append(CQLQuery(q2)).queryString shouldEqual s"$q1$q2"
    }
  }

  it should "append a string to a CQLQuery using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).append(q2).queryString shouldEqual s"$q1$q2"
    }
  }

  it should "append an escaped string to a CQLQuery using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).appendEscape(q2).queryString shouldEqual s"$q1`$q2`"
    }
  }

  it should "append an escaped query to another using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).appendEscape(CQLQuery(q2)).queryString shouldEqual s"$q1`$q2`"
    }
  }

  it should "append a list of query strings to a CQLQuery" in {
    forAll {(q1: String, queries: List[String]) =>
      val qb = queries.mkString(", ")
      CQLQuery(q1).append(queries).queryString shouldEqual s"$q1$qb"
    }
  }

  it should "terminate a query with ; if it doesn't already end with a semi colon" in {
    forAll {(q1: String) =>
      whenever(!q1.endsWith(CQLSyntax.Symbols.semicolon)) {
        CQLQuery(q1).terminate.queryString shouldEqual s"$q1;"
      }
    }
  }
}