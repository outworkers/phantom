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
package com.websudos.phantom.builder.query.db.crud

import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.outworkers.util.testing._

class ListOperatorsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.recipes.insertSchema()
  }

  it should "store items in a list in the same order" in {
    val recipe = gen[Recipe]

    val operation = for {
      truncate <- TestDatabase.recipes.truncate.future
      insertDone <- TestDatabase.recipes.store(recipe).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual recipe.ingredients
      }
    }
  }

  it should "store the same list size in Cassandra as it does in Scala" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual recipe.ingredients
        items.value should have size recipe.ingredients.size
      }
    }
  }

  it should "append an item to a list" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append "test").future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual recipe.ingredients ::: List("test")
      }
    }
  }

  it should "append several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual recipe.ingredients ::: appendable
      }
    }
  }

  it should "prepend an item to a list" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend "test").future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual List("test") :::  recipe.ingredients
      }
    }
  }

  it should "prepend several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")

    val prependedValues = if (cassandraVersion.value < Version.`2.0.13`) appendable.reverse else appendable

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend appendable).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual prependedValues ::: recipe.ingredients
      }
    }
  }

  it should "remove an item from a list" in {
    val list = genList[String]()
    val droppable = list(0)
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard droppable).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual list.tail
      }
    }
  }

  it should "remove multiple items from a list" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.tail).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value shouldEqual List(list.head)
      }
    }
  }

  it should "set a 0 index inside a List" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, "updated")).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.value.headOption.value shouldEqual "updated"
      }
    }
  }

  it should "set the third index inside a List" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- TestDatabase.recipes.store(recipe).future()
      update <- TestDatabase.recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, "updated")).future()
      select <- TestDatabase.recipes.select(_.ingredients).where(_.url eqs recipe.url).one()
    } yield select

    operation.successful {
      items => {
        items shouldBe defined
        items.value(3) shouldEqual "updated"
      }
    }
  }
}
