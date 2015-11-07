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
import com.websudos.phantom.builder.syntax.CQLSyntax
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

  it should "serialise a Where.contains clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.contains(column, value).queryString shouldEqual s"$column ${CQLSyntax.Operators.contains} $value"
  }

  it should "serialise a Where.containsKey clause" in {
    val column = gen[String]
    val value = gen[String]
    QueryBuilder.Where.containsKey(column, value).queryString shouldEqual s"$column ${CQLSyntax.Operators.containsKey} $value"
  }
}
