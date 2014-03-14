package com.newzly.phantom.dsl.crud

import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ TestRow, TestTable }
import com.newzly.util.finagle.AsyncAssertionsHelper._

class SetOperationsTest extends BaseTest {
  val keySpace = "setoperationstest"

  it should "append an item to a set column" in {

    TestTable.insertSchema

    val item = TestRow.sample()
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

  it should "append several items to a set column" in {

    TestTable.insertSchema

    val item = TestRow.sample()
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

  it should "remove an item from a set column" in {
    TestTable.insertSchema
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = TestRow.sample().copy(setText = someItems)
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

  it should "remove several items from a set column" in {
    TestTable.insertSchema
    val someItems = Set("test3", "test4", "test5", "test6")
    val item = TestRow.sample().copy(setText = someItems)
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

}
