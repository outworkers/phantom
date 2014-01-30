package com.newzly.phantom.dsl.crud

import java.net.InetAddress
import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions}
import org.scalatest.time.SpanSugar._
import com.datastax.driver.core.utils.UUIDs
import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables._
import com.newzly.phantom.tables.MyTestRow


class InsertTest  extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "InsertTestKeySpace"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)


  "Insert" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive.sample
    Primitives.insertSchema(session)
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
        .execute() flatMap {
          _ => {
            for {
              one <- Primitives.select.where(_.pkey eqs row.pkey).one
              multi <- Primitives.select.fetch
            } yield (one.get == row, multi contains row)
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
    TestTable.insertSchema(session)
    val row = TestRow.sample

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .execute() flatMap {
      _ => {
        for {
          one <- TestTable.select.where(_.key eqs row.key).one
          multi <- TestTable.select.fetch
        }  yield (one.get == row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  ignore should "work fine with custom types" in {
    MyTest.insertSchema(session)

    val row = MyTestRow.sample

    val rcp = MyTest.insert
      .value(_.key, row.key)
      .valueOrNull(_.optionA, row.optionA)
      .value(_.classS, row.classS)
      .execute() flatMap {
      _ =>  {
        for {
          one <- MyTest.select.one
          multi <- MyTest.select.fetch
        }  yield (one.get == row, multi.contains(row))
      }
    }
    rcp successful {
      res => {
        assert (res._1)
        assert (res._2)
      }
    }
  }

  ignore should "work fine with Mix" in {
    val r = Recipe.sample
    Recipes.insertSchema(session)
    val rcp = Recipes.insert
        .value(_.url, r.url)
        .valueOrNull(_.description, r.description)
        .value(_.ingredients, r.ingredients)
        .valueOrNull(_.author, r.author)
        .valueOrNull(_.servings, r.servings)
        .value(_.last_checked_at, r.lastCheckedAt)
        .value(_.props, r.props)
        .value(_.uid, UUIDs.timeBased()).execute() flatMap {
        _ => {
         Recipes.select.one
        }
      }

    rcp successful {
      res => {
        assert (res.get == r)
      }
    }
  }

  ignore should "support serializing/de-serializing empty lists " in {
    MyTest.insertSchema(session)

    val row = TestList.sample

    val f = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, row.l)
      .execute() flatMap {
      _ => MyTest.select.one
    }

    f successful  {
      res => res.isEmpty shouldEqual false
    }
  }

  ignore should "support serializing/de-serializing to List " in {
    MyTest.insertSchema(session)

    val row = TestList.sample

    val recipeF = MyTest.insert
      .value(_.key, row.key)
      .value(_.stringlist, row.l)
      .execute() flatMap {
      _ => MyTest.select.one
    }

    recipeF successful  {
      case res => {
        res.isEmpty shouldEqual false
        res.get should be(row)
      }
    }
  }

}
