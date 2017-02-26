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
package com.outworkers.phantom.builder.query

import com.outworkers.phantom.builder.query.engine.CQLQuery
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.testing._

class CQLQueryTest extends FlatSpec with Matchers {

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

}
