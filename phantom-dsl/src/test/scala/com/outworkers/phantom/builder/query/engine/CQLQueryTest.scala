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
import com.outworkers.util.samplers._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}


class CQLQueryTest extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig = PropertyCheckConfiguration(minSuccessful = 300)

  it should "create an empty CQL query using the empty method on the companion object" in {
    CQLQuery.empty.queryString shouldEqual ""
  }

  it should "automatically serialize a list of strings using the apply method from the companion object" in {
    val list = List("test", "test2")

    CQLQuery(list).queryString shouldEqual "test, test2"
  }

  it should "escape strings without single quotes inside them by wrapping the string in single quotes" in {
    val test = gen[String]
    CQLQuery.escape(test) shouldEqual s"'$test'"
  }

  it should "escape single quotes inside a string using the scape method of the companion" in {
    val test = "test'"
    CQLQuery.escape(test) shouldEqual "'test'''"
  }

  it should "correctly identify if a CQL query is empty" in {
    forAll {(q1: String) =>
      whenever(q1.nonEmpty) {
        CQLQuery(q1).nonEmpty shouldEqual true
      }
    }
  }

  it should "prepend one query to another using CQLQuery.prepend" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).prepend(CQLQuery(q2)).queryString shouldEqual s"$q2$q1"
    }
  }

  it should "prepend a string to a CQLQuery using CQLQuery.prepend" in {
    forAll {(q1: String, q2: String) =>
      CQLQuery(q1).prepend(q2).queryString shouldEqual s"$q2$q1"
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

  it should "prepend a string to another if the first string doesn't end with the other" in {
    forAll {(q1: String, q2: String) =>
      if(!q1.startsWith(q2)) {
        CQLQuery(q1).prependIfAbsent(q2).queryString shouldEqual s"$q2$q1"
      } else {
        CQLQuery(q1).prependIfAbsent(q2).queryString shouldEqual s"$q1"
      }
    }
  }

  it should "escape a CQL query by surrounding it with ` pairs" in {
    forAll { q1: String =>
      CQLQuery.empty.escape(q1) shouldEqual s"`$q1`"
    }
  }

  it should "correctly bpad a query if it doesn't end with a space" in {
    forAll { q1: String =>
      val q = CQLQuery(q1)
      if (q1.startsWith(" ")) {
        q.bpad.queryString shouldEqual q.queryString
      } else {
        q.bpad.queryString shouldEqual s" ${q.queryString}"
      }
    }
  }

  it should "correctly pad a query if it doesn't end with a space" in {
    forAll { q1: String =>
      val q = CQLQuery(q1)
      if (q1.endsWith(" ")) {
        q.pad.queryString shouldEqual q.queryString
      } else {
        q.pad.queryString shouldEqual s"${q.queryString} "
      }
    }
  }

  it should "correctly forcePad pad a query if it DOES end with a space" in {
    forAll { q1: String =>
      CQLQuery(q1).forcePad.queryString shouldEqual s"$q1 "
    }
  }

  it should "correctly trim a CQLQuery" in {
    forAll { q1: String =>
      CQLQuery(q1).trim.queryString shouldEqual s"${q1.trim}"
    }
  }

  it should "correctly identify if a query ends with a space" in {
    forAll { q1: String =>
      CQLQuery(q1).spaced shouldEqual q1.endsWith(" ")
    }
  }

  it should "single quote a CQL query by surrounding it with ' pairs" in {
    forAll { q1: String =>
      whenever(!q1.contains("'")) {
        CQLQuery.empty.singleQuote(q1) shouldEqual s"'$q1'"
      }
    }
  }

  it should "prepend a query to another if the first string doesn't end with the other" in {
    forAll {(q1: String, q2: String) =>
      if (!q1.startsWith(q2)) {
        CQLQuery(q1).prependIfAbsent(CQLQuery(q2)).queryString shouldEqual s"$q2$q1"
      } else {
        CQLQuery(q1).prependIfAbsent(CQLQuery(q2)).queryString shouldEqual s"$q1"
      }
    }
  }

  it should "append a string to another if the first string doesn't end with the other" in {
    forAll {(q1: String, q2: String) =>
      if(!q1.endsWith(q2)) {
        CQLQuery(q1).appendIfAbsent(q2).queryString shouldEqual s"$q1$q2"
      } else {
        CQLQuery(q1).appendIfAbsent(q2).queryString shouldEqual s"$q1"
      }
    }
  }

  it should "append a query to another if the first string doesn't end with the other" in {
    forAll {(q1: String, q2: String) =>
      if (!q1.endsWith(q2)) {
        CQLQuery(q1).appendIfAbsent(CQLQuery(q2)).queryString shouldEqual s"$q1$q2"
      } else {
        CQLQuery(q1).appendIfAbsent(CQLQuery(q2)).queryString shouldEqual s"$q1"
      }
    }
  }

  it should "append an single quoted string to a CQLQuery using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      whenever(!q2.contains("'")) {
        CQLQuery(q1).appendSingleQuote(q2).queryString shouldEqual s"$q1'$q2'"
      }
    }
  }

  it should "append an singlequoted query to another using CQLQuery.append" in {
    forAll {(q1: String, q2: String) =>
      whenever(!q2.contains("'")) {
        CQLQuery(q1).appendSingleQuote(CQLQuery(q2)).queryString shouldEqual s"$q1'$q2'"
      }
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

  it should "append and wrap a string with ()" in {
    forAll { (q1: String, q2: String) =>
      CQLQuery(q1).wrap(q2).queryString shouldEqual s"$q1 ($q2)"
    }
  }


  it should "append and wrap a CQLQuery with ()" in {
    forAll { (q1: String, q2: String) =>
      CQLQuery(q1).wrap(CQLQuery(q2)).queryString shouldEqual s"$q1 ($q2)"
    }
  }


  it should "append, pad and  wrap a list of query strings" in {
    forAll {(q1: String, queries: List[String]) =>
      val qb = queries.mkString(", ")
      CQLQuery(q1).wrap(queries).queryString shouldEqual s"$q1 ($qb)"
    }
  }

  it should "append, not pad and  wrap a list of query strings" in {
    forAll {(q1: String, queries: List[String]) =>
      val qb = queries.mkString(", ")
      CQLQuery(q1).wrapn(queries).queryString shouldEqual s"$q1($qb)"
    }
  }
}