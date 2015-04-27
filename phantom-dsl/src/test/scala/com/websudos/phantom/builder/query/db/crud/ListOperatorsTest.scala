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
package com.websudos.phantom.builder.query.db.crud

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class ListOperatorsTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Recipes.insertSchema()
  }

  it should "store items in a list in the same order" in {
    val recipe = gen[Recipe]
    val list = genList[String]()

    val operation = for {
      truncate <- Recipes.truncate.future
      insertDone <- Recipes.store(recipe).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients
      }
    }
  }

  it should "store items in a list in the same order with Twitter Futures" in {
    val recipe = gen[Recipe]

    val operation = for {
      truncate <- Recipes.truncate.execute
      insertDone <- Recipes.store(recipe).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients
      }
    }
  }

  it should "store the same list size in Cassandra as it does in Scala" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients
        items.get.size shouldEqual recipe.ingredients.size
      }
    }
  }

  it should "store the same list size in Cassandra as it does in Scala with Twitter Futures" in {
    val recipe = gen[Recipe]
    val limit = 5
    val list = genList[String](limit)

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients
        items.get.size shouldEqual recipe.ingredients.size
      }
    }
  }

  it should "append an item to a list" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append "test").future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: List("test")
      }
    }
  }

  it should "append an item to a list with Twitter futures" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append "test").execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: List("test")
      }
    }
  }

  it should "append several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: appendable
      }
    }
  }

  it should "append several items to a list with Twitter futures" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients append appendable).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual recipe.ingredients ::: appendable
      }
    }
  }

  it should "prepend an item to a list" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend "test").future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List("test") :::  recipe.ingredients
      }
    }
  }

  it should "prepend an item to a list with Twitter Futures" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend "test").execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List("test") :::  recipe.ingredients
      }
    }
  }

  it should "prepend several items to a list" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")
    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend appendable).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual appendable.reverse ::: recipe.ingredients
      }
    }
  }

  it should "prepend several items to a list with Twitter futures" in {
    val recipe = gen[Recipe]

    val appendable = List("test", "test2")

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients prepend appendable).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual appendable.reverse ::: recipe.ingredients
      }
    }
  }

  it should "remove an item from a list" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.head).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list.tail
      }
    }
  }

  it should "remove an item from a list with Twitter Futures" in {
    val list = genList[String]()

    val recipe = gen[Recipe].copy(ingredients = list)

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.head).execute
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual list.tail
      }
    }
  }

  it should "remove multiple items from a list" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val id = gen[UUID]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.tail).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List(list.head)
      }
    }
  }

  it should "remove multiple items from a list with Twitter futures" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val id = gen[UUID]

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients discard list.tail).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldEqual List(list.head)
      }
    }
  }

  it should "set a 0 index inside a List" in {
    val list = genList[String]()
    val recipe = gen[Recipe].copy(ingredients = list)
    val id = gen[UUID]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, "updated")).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(0) shouldEqual "updated"
      }
    }
  }

  it should "set an index inside a List with Twitter futures" in {

    val list = genList[String]()

    val recipe = gen[Recipe].copy(ingredients = list)
    val id = gen[UUID]

    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (0, "updated")).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(0) shouldEqual "updated"
      }
    }
  }

  it should "set the third index inside a List" in {
    val recipe = gen[Recipe]

    val operation = for {
      insertDone <- Recipes.store(recipe).future()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, "updated")).future()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).one
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(3) shouldEqual "updated"
      }
    }
  }

  it should "set the third index inside a List with Twitter Futures" in {
    val list = genList[String](100)
    val recipe = gen[Recipe]
    val id = gen[UUID]
    val updated = gen[String]


    val operation = for {
      insertDone <- Recipes.store(recipe).execute()
      update <- Recipes.update.where(_.url eqs recipe.url).modify(_.ingredients setIdx (3, updated)).execute()
      select <- Recipes.select(_.ingredients).where(_.url eqs recipe.url).get
    } yield select

    operation.successful {
      items => {
        items.isDefined shouldBe true
        items.get(3) shouldEqual updated
      }
    }
  }
}
