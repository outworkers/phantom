package com.newzly.phantom.dsl.crud

import org.scalatest.{ Assertions, Matchers }
import org.scalatest.concurrent.AsyncAssertions
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.helper.AsyncAssertionsHelper._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ JsonSeqColumnTable, SimpleStringModel }

class JsonSeqColumnTest extends BaseTest with Matchers with Assertions with AsyncAssertions {
  val keySpace = "basicInert"

  "JsonTypeSeqColumn" should "work fine for create" in {
    val insert = JsonSeqColumnTable.create(_.id, _.pkey, _.recipes).execute()

    insert successful {
      _ => info("table successful created")
    }
  }

  it should "work fine in insert" in {
    val table = JsonSeqColumnTable
    val createTask = table.create(_.id, _.pkey, _.recipes).execute()

    val resp = createTask flatMap {_=>
      info("table created")
      for {
        insertTask <- table.insert
          .value(_.id, UUIDs.timeBased())
          .value(_.pkey, "test")
          .value(_.recipes, Seq(SimpleStringModel("t1"), SimpleStringModel("t2"))).execute()
        selectTask <- table.select.one
      } yield selectTask
      ////////
      val i1 = table.insert
        .value(_.id, UUIDs.timeBased())
        .value(_.pkey, "test")
        .value(_.recipes, Seq.empty)

      i1.execute()

    }
    resp.sync()
  }

  it should "work fine in update" in {

  }

  it should "work fine in read" in {

  }

  it should "work fine in delete" in {

  }



}
