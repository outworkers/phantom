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

import java.util.UUID

import com.outworkers.phantom.builder.query.KeySpaceSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._
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
    Articles.select.where(_.id eqs someId).qb.queryString shouldBe s"SELECT * FROM phantom.articles WHERE id = $someId"
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
    query shouldEqual s"UPDATE phantom.articles SET name = 'test' WHERE id = $someId IF name = 'update'"
  }

  it should "serialize a 2 column partial select query" in {
    val someId = gen[UUID]
    Articles.select(_.id, _.name).where(_.id eqs someId).qb.queryString shouldBe s"SELECT id, name FROM phantom.articles WHERE id = $someId"
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
