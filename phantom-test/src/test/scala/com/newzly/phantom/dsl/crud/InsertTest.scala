package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables._
import com.newzly.util.finagle.AsyncAssertionsHelper._

class InsertTest extends BaseTest {
  val keySpace: String = "InsertTestKeySpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  "Insert" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive.sample
    Primitives.insertSchema()
    val rcp =  Primitives.insert
        .value(_.pkey, row.pkey)
        .value(_.long, row.long)
        .value(_.boolean, row.boolean)
        .value(_.bDecimal, row.bDecimal)
        .value(_.double, row.double)
        .value(_.float, row.float)
        .value(_.inet, row.inet)
        .value(_.int, row.int)
        .value(_.date, row.date)
        .value(_.uuid, row.uuid)
        .value(_.bi, row.bi)
        .future() flatMap {
          _ => {
            for {
              one <- Primitives.select.where(_.pkey eqs row.pkey).one
              multi <- Primitives.select.fetch
            } yield (one.get === row, multi contains row)
          }
       }

    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    TestTable.insertSchema()
    val row = TestRow.sample()

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .future() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).one
          multi <- TestTable.select.fetch
        }  yield (one.get === row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  it should "work fine with Mix" in {
    val r = Recipe.sample
    Recipes.insertSchema()
    val rcp = Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.servings, r.servings)
        .value(_.last_checked_at, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased()).future() flatMap {
        _ => {
         Recipes.select.one
        }
      }

    rcp successful {
      res => {
        assert (res.get === r)
      }
    }
  }

  it should "support serializing/de-serializing empty lists " in {
    MyTest.insertSchema()

    val row = MyTestRow.sample

    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, List.empty[String])
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    f successful  {
      res =>
        res.isEmpty shouldEqual false
        res.get.stringlist.isEmpty shouldEqual true
    }
  }

  it should "support serializing/de-serializing to List " in {
    MyTest.insertSchema()

    val row = MyTestRow.sample

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.optionA, row.optionA)
      .value(_.stringlist, row.stringlist)
      .future() flatMap {
      _ => MyTest.select.where(_.key eqs row.key).one
    }

    recipeF successful  {
      case res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }
  }

}
