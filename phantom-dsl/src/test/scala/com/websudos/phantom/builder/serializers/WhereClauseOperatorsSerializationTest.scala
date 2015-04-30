package com.websudos.phantom.builder.serializers

import com.websudos.phantom.builder.QueryBuilder
import com.websudos.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class WhereClauseOperatorsSerializationTest extends FlatSpec with Matchers {

  it should "serialise an Where.eqs clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.eqs(column, value).queryString shouldEqual s"$column = $value"
  }

  it should "serialise a Where.== clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.==(column, value).queryString shouldEqual s"$column = $value"
  }

  it should "serialise a Where.lt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.lt(column, value).queryString shouldEqual s"$column < $value"
  }

  it should "serialise a Where.lte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.lte(column, value).queryString shouldEqual s"$column <= $value"
  }

  it should "serialise a Where.gt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.gt(column, value).queryString shouldEqual s"$column > $value"
  }

  it should "serialise a Where.gte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.gte(column, value).queryString shouldEqual s"$column >= $value"
  }

  it should "serialise a Where.in clause" in {
    val column = gen[String]
    QueryBuilder.Where.in(column, "test", "test2").queryString shouldEqual s"$column IN (test, test2)"
  }
}
