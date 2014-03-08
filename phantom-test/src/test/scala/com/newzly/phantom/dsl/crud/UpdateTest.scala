package com.newzly.phantom.dsl.crud

import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.finagle.Implicits._
import com.newzly.util.finagle.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{
  Primitive,
  Primitives,
  TestRow,
  TestTable
}


class UpdateTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "UpdateTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(20 seconds)

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    val row = Primitive.sample

    val updatedRow = Primitive.sample.copy(pkey = row.pkey)
    Primitives.insertSchema(session)
    val rcp = Primitives.insert
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
        .value(_.bi, row.bi).future() flatMap {
        _ => {
          for {
            a <- Primitives.select.where(_.pkey eqs row.pkey).one
            b <- Primitives.select.fetch
            u <- Primitives.update.where(_.pkey eqs row.pkey)
                  .modify(_.long setTo updatedRow.long)
                  .and(_.boolean setTo updatedRow.boolean)
                  .and(_.bDecimal setTo updatedRow.bDecimal)
                  .and(_.double setTo updatedRow.double)
                  .and(_.float setTo updatedRow.float)
                  .and(_.inet setTo updatedRow.inet)
                  .and(_.int setTo updatedRow.int)
                  .and(_.date setTo updatedRow.date)
                  .and(_.uuid setTo updatedRow.uuid)
                  .and(_.bi setTo updatedRow.bi)
                  .future()
            a2 <- Primitives.select.where(_.pkey eqs row.pkey).one
            b2 <- Primitives.select.fetch

          } yield (
            a.get === row,
            b.contains(row),
            a2.get === updatedRow,
            b2.contains(updatedRow)
          )
        }
      }

    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
        assert(r._3)
        assert(r._4)
      }
    }
  }

  it should "work fine with List, Set, Map" in {

    val row = TestRow.sample

    val updatedRow = row.copy(
      list = List("new"),
      setText = Set("newSet"),
      mapTextToText =  Map("n" -> "newVal"),
      setInt = Set(3,4,7),
      mapIntToText = Map (-1 -> "&&&")
    )

    TestTable.insertSchema(session)

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .future() flatMap {
        _ => for {
        a <-TestTable.select.where(_.key eqs row.key).one
        b <-TestTable.select.fetch
        u <- TestTable.update
          .where(_.key eqs row.key)
          .modify(_.list setTo updatedRow.list)
          .and(_.setText setTo updatedRow.setText)
          .modify(_.mapTextToText setTo updatedRow.mapTextToText)
          .modify(_.setInt setTo updatedRow.setInt)
          .modify(_.mapIntToText setTo updatedRow.mapIntToText).future()
        a2 <- TestTable.select.where(_.key eqs row.key).one
        b2 <- TestTable.select.fetch
        } yield (
          a.get === row,
          b.contains(row),
          a2.get === updatedRow,
          b2.contains(updatedRow)
        )
    }
    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
        assert(r._3)
        assert(r._4)
      }
    }
  }

}
