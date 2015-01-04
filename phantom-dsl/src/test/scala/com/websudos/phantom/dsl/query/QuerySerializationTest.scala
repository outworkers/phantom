/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl.query

import java.util.UUID

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{Articles, Primitives, Recipes, TableWithCompoundKey}
import com.websudos.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class QuerySerializationTest extends FlatSpec with Matchers {

  it should "compile a full select query" in {
    "Articles.select.where(_.id eqs gen[UUID])" should compile
  }

  it should "correctly serialize a full select query" in {
    val someId = gen[UUID]
    Articles.select.where(_.id eqs someId).queryString shouldBe s"SELECT * FROM articles WHERE id=$someId;"
  }

  it should "compile a single column partial select query" in {
    "Articles.select(_.id).where(_.id eqs gen[UUID])" should compile
  }

  it should "correctly serialize a single column partial select query" in {
    val someId = gen[UUID]
    Articles.select(_.id).where(_.id eqs someId).queryString shouldBe s"SELECT id FROM ${Articles.tableName} WHERE id=$someId;"
  }

  it should "compile a query to query condition clause" in {
    """Articles.update.where(_.id eqs gen[UUID]).modify(_.name setTo "test").onlyIf(_.name eqs "update")""" should compile
  }

  it should "serialize a condition query to a query condition" in {
    val someId = gen[UUID]
    val query = Articles.update.where(_.id eqs someId).modify(_.name setTo "test").onlyIf(_.name eqs "update").queryString
    query shouldEqual s"UPDATE articles SET name='test' WHERE id=$someId IF name='update';"
  }

  it should "correctly serialize a 2 column partial select query" in {
    val someId = gen[UUID]
    Articles.select(_.id, _.name).where(_.id eqs someId).queryString shouldBe s"SELECT id,name FROM articles WHERE id=$someId;"
  }

  it should "correctly serialize a 3 column partial select query" in {
    val someId = gen[String]
    Recipes.select(
      _.url,
      _.description,
      _.ingredients
    ).where(_.url eqs someId).qb.toString shouldBe s"SELECT url,description,ingredients FROM Recipes WHERE url='$someId';"
  }

  it should "corectly serialise a simple conditional update query" in {
    val qb = Primitives.update.where(_.pkey eqs "test").onlyIf(_.boolean eqs false).queryString
    qb shouldEqual s"UPDATE Primitives WHERE pkey='test' IF boolean=false;"
  }

  it should "corectly serialise a multi-part conditional update query" in {
    val qb = Primitives.update.where(_.pkey eqs "test").onlyIf(_.boolean eqs false).and(_.long eqs 5L).qb.toString
    qb shouldEqual s"UPDATE Primitives WHERE pkey='test' IF boolean=false AND long=5;"
  }

  it should "corectly serialise a conditional update query with a single List column based clause" in {
    val qb = Recipes.update.where(_.url eqs "test")
      .modify(_.description setTo Some("blabla"))
      .onlyIf(_.ingredients eqs List("1", "2", "3"))
      .queryString

    qb shouldEqual "UPDATE Recipes SET description='blabla' WHERE url='test' IF ingredients=['1','2','3'];"
  }

  it should "corectly serialise a multi-part conditional update query with a List column part" in {
    val qb = Recipes.update.where(_.url eqs "test")
      .modify(_.description setTo Some("blabla"))
      .onlyIf(_.ingredients eqs List("1", "2", "3"))
      .and(_.description eqs Some("test"))
      .queryString

    qb shouldEqual "UPDATE Recipes SET description='blabla' WHERE url='test' IF ingredients=['1','2','3'] AND description='test';"
  }

  it should "serialize a simple count query" in {
    Recipes.count.queryString shouldEqual "SELECT count(*) FROM Recipes;"
  }

  it should "serialize a count query with a where clause" in {
    val key = gen[String]
    Recipes.count.where(_.url eqs key).queryString shouldEqual s"SELECT count(*) FROM Recipes WHERE url='$key';"
  }

  it should "serialize a count query with a where-and clause" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.count.where(_.id eqs id).and(_.second eqs id).queryString shouldEqual s"SELECT count(*) FROM TableWithCompoundKey WHERE id=$key AND second=$key;"
  }

  it should "allow setting a limit on a count query" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.count.where(_.id eqs id).and(_.second eqs id).limit(10).queryString shouldEqual s"SELECT count(*) FROM TableWithCompoundKey WHERE id=$key AND second=$key LIMIT 10;"
  }

  it should "allow filtering on a count query" in {
    val id = UUID.randomUUID()
    val key = id.toString
    TableWithCompoundKey.count.allowFiltering().where(_.id eqs id).and(_.second eqs id).limit(10).queryString shouldEqual s"SELECT count(*) FROM TableWithCompoundKey WHERE id=$key AND second=$key LIMIT 10 ALLOW FILTERING;"
  }

}
