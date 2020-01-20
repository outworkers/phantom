/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class SetOperationsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.testTable.createSchema()
  }

  it should "append an item to a set column" in {
    val item = gen[TestRow]
    val someItem = "test5"

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- database.testTable.update.where(_.key eqs item.key).modify(_.setText add someItem).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe item.setText + someItem
    }
  }


  it should "append an item to a set column using prepared statements" in {
    val item = gen[TestRow]
    val someItem = "test5"

    val query = database.testTable.update
      .where(_.key eqs ?)
      .modify(_.setText add ?)
      .prepareAsync()

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- query.flatMap(_.bind(Set(someItem), item.key).future())
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe item.setText + someItem
    }
  }


  it should "append several items to a set column" in {
    val item = gen[TestRow]
    val someItems = Set("test5", "test6")

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- database.testTable.update.where(_.key eqs item.key).modify(_.setText addAll someItems).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe item.setText ++ someItems
    }
  }

  it should "append several items to a set column using prepared queries" in {
    val item = gen[TestRow]
    val someItems = Set("test5", "test6")
    val query = database.testTable.update.where(_.key eqs ?).modify(_.setText addAll ?)

    val chain = for {
      _ <- database.testTable.store(item).future()
      bindable <- query.prepareAsync()
      _ <- bindable.bind(someItems, item.key).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe item.setText ++ someItems
    }
  }


  it should "remove an item from a set column" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = "test6"

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- database.testTable.update.where(_.key eqs item.key).modify(_.setText remove removal).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(Set(removal))
    }
  }

  it should "remove an item from a set column using prepared statements" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = "test6"

    val query = database.testTable.update
      .where(_.key eqs ?)
      .modify(_.setText remove ?)
      .prepareAsync()

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- query.flatMap(_.bind(Set(removal), item.key).future())
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(Set(removal))
    }
  }

  it should "remove an item from a set column using prepared queries and delete syntax" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = "test6"

    val query = database.testTable.deleteP(_.setText(?)).where(_.key eqs ?)

    val chain = for {
      _ <- database.testTable.store(item).future()
      updatePrep <- query.prepareAsync()
      _ <- updatePrep.bind(removal, item.key).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe (someItems - removal)
    }
  }

  it should "remove several items from a set column" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = Set("test5", "test6")

    val chain = for {
      _ <- database.testTable.store(item).future()
      _ <- database.testTable.update.where(_.key eqs item.key).modify(_.setText removeAll removal).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(removal)
    }
  }

  it should "remove several items from a set column using prepared queries" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = Set("test5", "test6")

    val query = database.testTable.update.where(_.key eqs ?).modify(_.setText removeAll ?)

    val chain = for {
      _ <- database.testTable.store(item).future()
      bindable <- query.prepareAsync()
      _ <- bindable.bind(removal, item.key).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(removal)
    }
  }
}
