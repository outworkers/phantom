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
import com.outworkers.phantom.builder.syntax.CQLSyntax
import com.outworkers.util.samplers._
import org.scalatest.{FlatSpec, Matchers}

class WhereClauseOperatorsSerializationTest extends FlatSpec with Matchers {

  "The Where.Builder" should "serialise an Where.eqs clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.eqs(column, value).queryString shouldEqual s"$column = $value"
  }

  "The Where.Builder" should "serialise a Where.== clause" in {

    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.eqs(column, value).queryString shouldEqual s"$column = $value"
  }

  "The Where.Builder" should "serialise a Where.lt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.lt(column, value).queryString shouldEqual s"$column < $value"
  }

  "The Where.Builder" should "serialise a Where.lte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.lte(column, value).queryString shouldEqual s"$column <= $value"
  }

  "The Where.Builder" should "serialise a Where.gt clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.gt(column, value).queryString shouldEqual s"$column > $value"
  }

  "The Where.Builder" should "serialise a Where.gte clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.gte(column, value).queryString shouldEqual s"$column >= $value"
  }

  "The Where.Builder" should "serialise a Where.in clause" in {
    val column = gen[String]
    QueryBuilder.Where.in(column, "test", "test2").queryString shouldEqual s"$column IN (test, test2)"
  }

  "The Where.Builder" should "serialise a Where.contains clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.contains(column, value).queryString shouldEqual s"$column ${CQLSyntax.Operators.contains} $value"
  }

  "The Where.Builder" should "serialise a Where.containsKey clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.containsKey(column, value).queryString shouldEqual s"$column ${CQLSyntax.Operators.containsKey} $value"
  }

  "The Where.Builder" should "serialise a Where.containsEntry clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.containsEntry(column, "'test'", value).queryString shouldEqual s"$column['test'] ${CQLSyntax.Operators.eqs} $value"
  }
}
