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
package com.websudos.phantom.dsl.crud

import com.websudos.phantom.dsl._
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
