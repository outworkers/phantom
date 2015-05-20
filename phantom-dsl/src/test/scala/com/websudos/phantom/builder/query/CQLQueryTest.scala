package com.websudos.phantom.builder.query

import org.scalatest.{Matchers, FlatSpec}
import com.websudos.util.testing._

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
