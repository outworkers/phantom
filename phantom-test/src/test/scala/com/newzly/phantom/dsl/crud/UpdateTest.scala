package com.newzly.phantom.dsl.crud

import java.net.InetAddress
import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.{ AsyncAssertions, PatienceConfiguration }
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper._
import com.newzly.phantom.helper.Primitive

class UpdateTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace: String = "UpdateTest"
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java
    object Primitives extends Primitives {
      override def tableName = "PrimitivesUpdateTest"
    }

    val row = Primitive("myStringUpdate", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002))

    val updatedRow = Primitive("myStringUpdate", 21.toLong, true, BigDecimal("11.11"), 31.toDouble, 41.toFloat,
      InetAddress.getByName("127.1.1.1"), 911, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1012))

    val rcp = Primitives.create(_.pkey,
      _.long,
      _.boolean,
      _.bDecimal,
      _.double,
      _.float,
      _.inet,
      _.int,
      _.date,
      _.uuid,
      _.bi)
      .execute() flatMap {
        _ =>Primitives.insert
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
        .value(_.bi, row.bi).execute()
      } flatMap {
      _ => {
          for {
            a <- Primitives.select.where(_.pkey eqs "myStringUpdate").one
            b <- Primitives.select.fetch
            u <- Primitives.update.where(_.pkey eqs "myStringUpdate")
                  .modify(_.long, updatedRow.long)
                  .modify(_.boolean, updatedRow.boolean)
                  .modify(_.bDecimal, updatedRow.bDecimal)
                  .modify(_.double, updatedRow.double)
                  .modify(_.float, updatedRow.float)
                  .modify(_.inet, updatedRow.inet)
                  .modify(_.int, updatedRow.int)
                  .modify(_.date, updatedRow.date)
                  .modify(_.uuid, updatedRow.uuid)
                  .modify(_.bi, updatedRow.bi).execute()
            a2 <- Primitives.select.where(_.pkey eqs "myStringUpdate").one
            b2 <- Primitives.select.fetch

          } yield (
            a.get == row,
            b.contains(row),
            a2.get == updatedRow,
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

    val row = TableHelper.getAUniqueJsonTestRow

    val updatedRow = row.copy(
      list = Seq ("new"),
      setText = Set("newSet"),
      mapTextToText =  Map("n" -> "newVal"),
      setInt = Set(3,4,7),
      mapIntToText = Map (-1 -> "&&&")
    )

    object TestTable extends TestTable {
      override def tableName = "UpdateTestTable"
    }

    val createTestTable =
      """|CREATE TABLE UpdateTestTable(
        |key text PRIMARY KEY,
        |list list<text>,
        |setText set<text>,
        |mapTextToText map<text,text>,
        |setInt set<int>,
        |mapIntToText map<int,text> );
      """.stripMargin
    session.execute(createTestTable)

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
      .execute() flatMap {
        _ => for {
        a <-TestTable.select.where(_.key eqs "w").one
        b <-TestTable.select.fetch
        u <- TestTable.update
          .where(_.key eqs "w")
          .modify(_.list,updatedRow.list)
          .modify(_.setText,updatedRow.setText)
          .modify(_.mapTextToText,updatedRow.mapTextToText)
          .modify(_.setInt,updatedRow.setInt)
          .modify(_.mapIntToText,updatedRow.mapIntToText).execute()
        a2 <- TestTable.select.where(_.key eqs "w").one
        b2 <- TestTable.select.fetch
        } yield (
          a.get == row,
          b.contains(row),
          a2.get == updatedRow,
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
