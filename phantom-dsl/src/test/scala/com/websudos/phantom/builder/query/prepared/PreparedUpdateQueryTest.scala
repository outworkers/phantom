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
package com.websudos.phantom.builder.query.prepared

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.tables.Recipe
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class PreparedUpdateQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.insertSchema()
  }

  it should "execute a prepared update query with a single argument bind" in {

    val updated = genOpt[ShortString].map(_.value)

    val query = database.recipes.update
      .p_where(_.url eqs ?)
      .p_modify(_.description setTo ?)
      .prepare()

    val recipe = gen[Recipe]

    val chain = for {
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- query.bind(recipe.url, updated).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, afterUpdate) => {
        initial shouldBe defined
        initial.value shouldEqual recipe

        afterUpdate shouldBe defined
        afterUpdate.value.url shouldEqual recipe.url
        afterUpdate.value.props shouldEqual recipe.props
        afterUpdate.value.ingredients shouldEqual recipe.ingredients
        afterUpdate.value.servings shouldEqual recipe.servings
        afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
        afterUpdate.value.uid shouldEqual recipe.uid
        afterUpdate.value.description shouldEqual recipe.description
      }
    }
  }

  it should "execute a prepared update query with a three argument bind" in {

    val updated = genOpt[ShortString].map(_.value)
    val updatedServings = gen[UUID]

    val query = database.recipes.update
      .p_where(_.url eqs ?)
      .p_modify(_.description setTo ?)
      .p_and(_.uid setTo ?)
      .prepare()

    val recipe = gen[Recipe]

    val chain = for {
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- query.bind(recipe.url, updated, updatedServings).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, afterUpdate) => {
        initial shouldBe defined
        initial.value shouldEqual recipe

        afterUpdate shouldBe defined
        afterUpdate.value.url shouldEqual recipe.url
        afterUpdate.value.props shouldEqual recipe.props
        afterUpdate.value.ingredients shouldEqual recipe.ingredients
        afterUpdate.value.servings shouldEqual updatedServings
        afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
        afterUpdate.value.uid shouldEqual recipe.uid
        afterUpdate.value.description shouldEqual recipe.description
      }
    }
  }

  ignore should "execute a prepared update query with a three argument bind and a TTL clause" in {

    val updated = genOpt[ShortString].map(_.value)
    val updatedServings = gen[UUID]

    val query = database.recipes.update
      .p_where(_.url eqs ?)
      .p_modify(_.description setTo ?)
      .p_and(_.uid setTo ?)
      .p_ttl(?)
      .prepare()

    val recipe = gen[Recipe]

    val chain = for {
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      update <- query.bind(recipe.url, updated, updatedServings, 5L).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, afterUpdate) => {
        initial shouldBe defined
        initial.value shouldEqual recipe

        afterUpdate shouldBe defined
        afterUpdate.value.url shouldEqual recipe.url
        afterUpdate.value.props shouldEqual recipe.props
        afterUpdate.value.ingredients shouldEqual recipe.ingredients
        afterUpdate.value.servings shouldEqual updatedServings
        afterUpdate.value.lastCheckedAt shouldEqual recipe.lastCheckedAt
        afterUpdate.value.uid shouldEqual recipe.uid
        afterUpdate.value.description shouldEqual recipe.description
      }
    }
  }

}
