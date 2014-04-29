package com.newzly.phantom.dsl.batch

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ JodaRow, PrimitivesJoda }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest

class BatchTruncateTest extends BaseTest {
  val keySpace = "batch_truncate_test"

  it should "truncate multiple tables in a single batch" in {
    val row = JodaRow.sample
    val row2 = JodaRow.sample.copy(pkey = row.pkey)
    val row3 = JodaRow.sample
    PrimitivesJoda.insertSchema()
    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)
    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)
    val statement3 = PrimitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .modify(_.timestamp setTo  row2.bi)
    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = new BatchStatement()
      .add(statement1)
      .add(statement2)
      .add(statement3)
      .add(statement4)

    val chain = for {
      b <- batch.execute()
      count <- PrimitivesJoda.count.get()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.isDefined shouldEqual true
        res.get.get shouldEqual 1L
      }
    }

  }
}
