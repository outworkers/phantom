package com.websudos.phantom.builder

import org.scalatest.{Matchers, FlatSpec}
import com.websudos.util.testing._

class QueryBuilderTest extends FlatSpec with Matchers {


  it should "serialise an Where.eqs clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.eqs(column, value).queryString shouldEqual s"$column = $value"
  }

  it should "serialise a Where.== clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.==(column, value).queryString shouldEqual s"$column = $value"
  }


  it should "serialise a Where.lt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.lt(column, value).queryString shouldEqual s"$column < $value"
  }

  it should "serialise a Where.lte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.lte(column, value).queryString shouldEqual s"$column <= $value"
  }

  it should "serialise a Where.gt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.gt(column, value).queryString shouldEqual s"$column > $value"
  }

  it should "serialise a Where.gte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.gte(column, value).queryString shouldEqual s"$column >= $value"
  }

  it should "serialise a Where.in clause" in {
    val column = gen[String]
    QueryBuilder.in(column, "test", "test2").queryString shouldEqual s"$column IN (test, test2)"
  }
}
