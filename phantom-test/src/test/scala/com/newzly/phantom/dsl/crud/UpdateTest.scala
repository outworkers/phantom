package com.newzly.phantom.dsl.crud

import com.newzly.phantom.dsl.BaseTest
import org.scalatest.{Assertions, Matchers}
import com.newzly.phantom.helper.Tables
import com.datastax.driver.core.{Row, Session}
import java.net.InetAddress
import com.twitter.util.Future
import com.newzly.phantom._
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import org.scalatest.concurrent.AsyncAssertions


class UpdateTest extends BaseTest with Matchers with Tables  with Assertions with AsyncAssertions {

  implicit val session: Session = cassandraSession

  "Update" should "work fine for primitives columns" in {
    //char is not supported
    //https://github.com/datastax/java-driver/blob/2.0/driver-core/src/main/java/com/datastax/driver/core/DataType.java

    val row = Primitive("myStringUpdate", 2.toLong, true, BigDecimal("1.1"), 3.toDouble, 4.toFloat,
      InetAddress.getByName("127.0.0.1"), 9, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
      BigInt(1002))
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
      .value(_.bi, row.bi)
    rcp.execute() map {
      _ => {
        val recipeF: Future[Option[Primitive]] = Primitives.select.where(_.pkey eqs "myStringUpdate").one
        recipeF successful {
          case res => assert(res.get === row)
        }
        Primitives.select.fetch successful {
          case res => assert(res.contains(row))
        }

        val updatedRow = Primitive("myStringUpdate", 21.toLong, true, BigDecimal("11.11"), 31.toDouble, 41.toFloat,
          InetAddress.getByName("127.1.1.1"), 911, new java.util.Date, com.datastax.driver.core.utils.UUIDs.timeBased(),
          BigInt(1012))

        Primitives.update
        .where(_.pkey eqs "myStringUpdate")
        .modify(_.long, updatedRow.long)
        .modify(_.boolean, updatedRow.boolean)
        .modify(_.bDecimal, updatedRow.bDecimal)
        .modify(_.double, updatedRow.double)
        .modify(_.float, updatedRow.float)
        .modify(_.inet, updatedRow.inet)
        .modify(_.int, updatedRow.int)
        .modify(_.date, updatedRow.date)
        .modify(_.uuid, updatedRow.uuid)
        .modify(_.bi, updatedRow.bi).execute() map {
          _ => {
            val recipeF2: Future[Option[Primitive]] = Primitives.select.where(_.pkey eqs "myStringUpdate").one
            recipeF2 successful {
              case res => assert(res.get === updatedRow)
            }
            Primitives.select.fetch successful {
              case res => assert(res.contains(updatedRow))
            }
          }
        }
      }
    }
  }

  it should "work fine with List, Set, Map" in {
    val row = TestRow("w", Seq("ee", "pp", "ee3"), Set("u", "e"), Map("k" -> "val"), Set(1, 22, 2),
      Map(3 -> "OO"))

    val rcp = TestTable.insert
      .value(_.key, row.key)
      .value(_.list, row.list)
      .value(_.setText, row.setText)
      .value(_.mapTextToText, row.mapTextToText)
      .value(_.setInt, row.setInt)
      .value(_.mapIntToText, row.mapIntToText)
    rcp.execute() map {
      _ => {
        val recipeF: Future[Option[TestRow]] = TestTable.select.where(_.key eqs "w").one
        recipeF successful {
          case res => assert(res.get === row)
        }
        TestTable.select.fetch successful {
          case res => assert(res.contains(row))
        }

        val updatedRow = row.copy(
          list = Seq ("new"),
          setText = Set("newSet"),
          mapTextToText =  Map("n" -> "newVal"),
          setInt = Set(3,4,7),
          mapIntToText = Map (-1 -> "&&&")
        )

        TestTable.update
          .where(_.key eqs "w")
          .modify(_.list,updatedRow.list)
          .modify(_.setText,updatedRow.setText)
          .modify(_.mapTextToText,updatedRow.mapTextToText)
          .modify(_.setInt,updatedRow.setInt)
          .modify(_.mapIntToText,updatedRow.mapIntToText).execute() map {
            _ => {
              val recipeF2: Future[Option[TestRow]] = TestTable.select.where(_.key eqs "w").one
              recipeF2 successful {
                case res => assert(res.get === updatedRow)
              }
              TestTable.select.fetch successful {
                case res => assert(res.contains(updatedRow))
              }
            }
          }
      }
    }
  }
}
