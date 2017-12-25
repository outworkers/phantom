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
      insertDone <- database.testTable.store(item).future()
      update <- database.testTable.update.where(_.key eqs item.key).modify(_.setText add someItem).future()
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
      insertDone <- database.testTable.store(item).future()
      update <- database.testTable.update.where(_.key eqs item.key).modify(_.setText addAll someItems).future()
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
      insertDone <- database.testTable.store(item).future()
      update <- database.testTable.update.where(_.key eqs item.key).modify(_.setText remove removal).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(Set(removal))
    }
  }

  it should "remove several items from a set column" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = Set("test5", "test6")

    val chain = for {
      insertDone <- database.testTable.store(item).future()
      update <- database.testTable.update.where(_.key eqs item.key).modify(_.setText removeAll removal).future()
      db <- database.testTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    whenReady(chain) { items =>
      items.value shouldBe someItems.diff(removal)
    }
  }
}
