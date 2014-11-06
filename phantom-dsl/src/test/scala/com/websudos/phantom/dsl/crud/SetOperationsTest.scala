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
package com.websudos.phantom.dsl.crud

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class SetOperationsTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestTable.insertSchema()
  }

  it should "append an item to a set column" in {
    val item = gen[TestRow]
    val someItem = "test5"

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .future()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText add someItem).future()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe item.setText + someItem
      }
    }
  }

  it should "append an item to a set column with Twitter Futures" in {
    val item = gen[TestRow]
    val someItem = "test5"

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .execute()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText add someItem).execute()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).get()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe item.setText + someItem
      }
    }
  }

  it should "append several items to a set column" in {
    val item = gen[TestRow]
    val someItems = Set("test5", "test6")

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .future()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText addAll someItems).future()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe item.setText ++ someItems
      }
    }
  }

  it should "append several items to a set column with Twitter Futures" in {
    val item = gen[TestRow]
    val someItems = Set("test5", "test6")

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .execute()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText addAll someItems).execute()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).get()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe item.setText ++ someItems
      }
    }
  }

  it should "remove an item from a set column" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = "test6"

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .future()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText remove removal).future()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe someItems.diff(Set(removal))
      }
    }
  }

  it should "remove an item from a set column with Twitter Futures" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = "test6"

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .execute()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText remove removal).execute()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).get()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe someItems.diff(Set(removal))
      }
    }
  }

  it should "remove several items from a set column" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = Set("test5", "test6")

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .future()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText removeAll removal).future()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).one()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe someItems.diff(removal)
      }
    }
  }

  it should "remove several items from a set column with Twitter Futures" in {
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = gen[TestRow].copy(setText = someItems)
    val removal = Set("test5", "test6")

    val insert = TestTable.insert
      .value(_.key, item.key)
      .value(_.list, item.list)
      .value(_.setText, item.setText)
      .value(_.setInt, item.setInt)
      .value(_.mapTextToText, item.mapTextToText)
      .value(_.mapIntToText, item.mapIntToText)
      .execute()

    val chain = for {
      insertDone <- insert
      update <- TestTable.update.where(_.key eqs item.key).modify(_.setText removeAll removal).execute()
      db <- TestTable.select(_.setText).where(_.key eqs item.key).get()
    } yield db

    chain.successful {
      items => {
        items.isDefined shouldBe true
        items.get shouldBe someItems.diff(removal)
      }
    }
  }
}
