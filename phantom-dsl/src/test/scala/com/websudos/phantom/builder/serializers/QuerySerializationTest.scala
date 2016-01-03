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

import java.util.UUID

import com.websudos.phantom.builder.query.KeySpaceSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.websudos.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class QuerySerializationTest extends FlatSpec with Matchers with KeySpaceSuite {

  val Articles = TestDatabase.articles
  val Recipes = TestDatabase.recipes
  val TableWithCompoundKey = TestDatabase.tableWithCompoundKey

  it should "compile a full select query" in {
    "Articles.select.where(_.id eqs gen[UUID])" should compile
  }

  it should "serialize a full select query" in {
    val someId = gen[UUID]
    Articles.select.where(_.id eqs someId).qb.queryString shouldBe s"SELECT * FROM phantom.Articles WHERE id = $someId"
  }

  it should "compile a single column partial select query" in {
    "Articles.select(_.id).where(_.id eqs gen[UUID])" should compile
  }

  it should "serialize a single column partial select query" in {
    val someId = gen[UUID]
    Articles.select(_.id).where(_.id eqs someId).qb.queryString shouldBe s"SELECT id FROM phantom.${Articles.tableName} WHERE id = $someId"
  }

  it should "compile a query to query condition clause" in {
    """Articles.update.where(_.id eqs gen[UUID]).modify(_.name setTo "test").onlyIf(_.name is "update")""" should compile
  }

  it should "serialize a condition query to a query condition" in {
    val someId = gen[UUID]
    val query = Articles.update.where(_.id eqs someId).modify(_.name setTo "test").onlyIf(_.name is "update").qb.queryString
    query shouldEqual s"UPDATE phantom.Articles SET name = 'test' WHERE id = $someId IF name = 'update'"
  }

  it should "serialize a 2 column partial select query" in {
    val someId = gen[UUID]
    Articles.select(_.id, _.name).where(_.id eqs someId).qb.queryString shouldBe s"SELECT id, name FROM phantom.Articles WHERE id = $someId"
  }

  it should "serialize a 3 column partial select query" in {
    val someId = gen[String]
    Recipes.select(
      _.url,
      _.description,
      _.ingredients
    ).where(_.url eqs someId)
      .qb.queryString shouldBe s"SELECT url, description, ingredients FROM phantom.recipes WHERE url = '$someId'"
  }

  it should "serialise a conditional update query with a single List column based clause" in {
    val qb = Recipes.update.where(_.url eqs "test")
      .modify(_.description setTo Some("blabla"))
      .onlyIf(_.ingredients is List("1", "2", "3"))
      .qb.queryString

    qb shouldEqual "UPDATE phantom.recipes SET description = 'blabla' WHERE url = 'test' IF ingredients = ['1', '2', '3']"
  }

  it should "serialise a multi-part conditional update query with a List column part" in {
    val qb = Recipes.update.where(_.url eqs "test")
      .modify(_.description setTo Some("blabla"))
      .onlyIf(_.ingredients is List("1", "2", "3"))
      .and(_.description is Some("test"))
      .qb.queryString

    qb shouldEqual "UPDATE phantom.recipes SET description = 'blabla' WHERE url = 'test' IF ingredients = ['1', '2', '3'] AND description = 'test'"
  }

  it should "serialize a simple count query" in {
    Recipes.select.count.qb.queryString shouldEqual "SELECT COUNT(*) FROM phantom.recipes"
  }

  it should "serialize a count query with a where clause" in {
    val key = gen[String]
    Recipes.select.count.where(_.url eqs key).qb.queryString shouldEqual s"SELECT COUNT(*) FROM phantom.recipes WHERE url = '$key'"
  }

  it should "serialize a count query with a where-and clause" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.select.count.where(_.id eqs id).and(_.second eqs id).qb.queryString shouldEqual s"SELECT COUNT(*) FROM phantom.tableWithCompoundKey WHERE id = $key AND second = $key"
  }

  it should "allow setting a limit on a count query" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.select.count.where(_.id eqs id).and(_.second eqs id).limit(10).qb.queryString shouldEqual s"SELECT COUNT(*) FROM phantom.tableWithCompoundKey WHERE id = $key AND second = $key LIMIT 10"
  }

  it should "allow filtering on a count query" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.select.count
      .where(_.id eqs id).and(_.second eqs id)
      .limit(10)
      .allowFiltering()
      .qb.queryString shouldEqual s"SELECT COUNT(*) FROM phantom.tableWithCompoundKey WHERE id = $key AND second = $key LIMIT 10 ALLOW FILTERING"
  }

}
