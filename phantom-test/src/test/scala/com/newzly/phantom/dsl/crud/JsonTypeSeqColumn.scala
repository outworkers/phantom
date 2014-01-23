package com.newzly.phantom.dsl.crud

import com.twitter.conversions.time._
import org.scalatest.{Assertions, Matchers}
import com.newzly.phantom.helper.{BaseTest, Tables}
import org.scalatest.concurrent.AsyncAssertions
import com.newzly.phantom.{PrimitiveColumn, CassandraTable, JsonTypeSeqColumn}
import com.datastax.driver.core.Row
import com.newzly.phantom.field.{LongOrderKey, UUIDPk}
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.twitter.util.{Await, Future}
import com.datastax.driver.core.utils.UUIDs

class JsonTypeSeqColumnTest extends BaseTest with Matchers with Tables with Assertions with AsyncAssertions{
  //Use this to debug only
  implicit class SyncFuture[T](future: Future[T]) {
    def sync(): T = {
      Await.result(future, 10.seconds)
    }
  }
  val keySpace = "basicInert"

  case class T(something: String)

  case class JsonTypeSeqColumnRow(pkey: String, jtsc: Seq[T])

  class JsonTypeSeqColumnTable extends CassandraTable[JsonTypeSeqColumnTable,JsonTypeSeqColumnRow] with UUIDPk[JsonTypeSeqColumnTable]
  with LongOrderKey[JsonTypeSeqColumnTable] {
    override def fromRow(r: Row): JsonTypeSeqColumnRow = {
      JsonTypeSeqColumnRow(pkey(r),jtsc(r))
    }
    object pkey extends PrimitiveColumn[String]
    object jtsc extends JsonTypeSeqColumn[T]
  }
  object JsonTypeSeqColumnTable extends JsonTypeSeqColumnTable {
    def apply(_tableName: String) = new JsonTypeSeqColumnTable {override val tableName=_tableName}
  }

  "JsonTypeSeqColumn" should "work fine for create" in {
    val insert = JsonTypeSeqColumnTable("JsonTypeSeqColumnTableCreate").create(_.id, _.pkey, _.jtsc).execute()

    insert successful {
      _ => info("table successful created")
    }
  }

  it should "work fine in insert" in {
    val table = JsonTypeSeqColumnTable("JsonTypeSeqColumnTableInsert")
    val createTask = table.create(_.id, _.pkey, _.jtsc).execute()

    val resp = createTask flatMap {_=>
      info("table created")
      for {
        insertTask <- table.insert
          .value(_.id, UUIDs.timeBased())
          .value(_.pkey, "test")
          .value(_.jtsc, Seq(T("t1"),T("t2"))).execute()
        selectTask <- table.select.one
      } yield (selectTask)
      ////////
      val i1 = table.insert
        .value(_.id, UUIDs.timeBased())
        .value(_.pkey, "test")
        .value(_.jtsc, Seq.empty)

      i1.execute()

    }


    resp.sync()
    /*resp successful {
      r => r.get match {
        case row: JsonTypeSeqColumnRow => assert(row.pkey == "test")
        case _ => fail("unexpected result returned from cassandra")
      }
    }*/
  }

  it should "work fine in update" in {

  }

  it should "work fine in read" in {

  }

  it should "work fine in delete" in {

  }



}
