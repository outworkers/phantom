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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.outworkers.phantom.builder.query.prepared

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.{Article, Recipe}
import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._

class PreparedDeleteQueryTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.insertSchema()
    database.articlesByAuthor.insertSchema()
  }

  it should "correctly execute a prepared delete query" in {
    val recipe = gen[Recipe]

    val query = database.recipes.delete.where(_.url eqs ?).prepare()

    val chain = for {
      store <- database.recipes.store(recipe).future()
      get <- database.recipes.select.where(_.url eqs recipe.url).one()
      delete <- query.bind(recipe.url).future()
      get2 <- database.recipes.select.where(_.url eqs recipe.url).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, afterDelete) => {
        initial shouldBe defined
        initial.value shouldEqual recipe
        afterDelete shouldBe empty
      }
    }
  }

  it should "correctly execute a prepared delete query with 2 bound values" in {
    val sample = database.multipleKeysTable$

    val author = gen[UUID]
    val cat = gen[UUID]
    val article = gen[Article]

    val query = database.articlesByAuthor.delete
      .where(_.category eqs ?)
      .and(_.author_id eqs ?)
      .prepare()

    val chain = for {
      store <- database.articlesByAuthor.store(author, cat, article).future()
      get <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
      delete <- query.bind(cat, author).future()
      get2 <- database.articlesByAuthor.select.where(_.category eqs cat).and(_.author_id eqs author).one()
    } yield (get, get2)

    whenReady(chain) {
      case (initial, afterDelete) => {
        initial shouldBe defined
        initial.value shouldEqual article
        afterDelete shouldBe empty
      }
    }

  }

}
