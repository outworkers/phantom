package com.websudos.phantom.builder.batch

import org.joda.time.DateTime
import com.websudos.phantom.dsl.Batch
import com.websudos.phantom.tables.{PrimitivesJoda, JodaRow}
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.phantom.dsl._
import com.websudos.util.testing._

class BatchQueryTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    PrimitivesJoda.insertSchema()
  }

  it should "correctly execute a chain of INSERT queries" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = Batch.logged.add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.select.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 3
      }
    }
  }

  it should "correctly execute a chain of INSERT queries with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow]
    val row3 = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val statement2 = PrimitivesJoda.insert
      .value(_.pkey, row2.pkey)
      .value(_.intColumn, row2.int)
      .value(_.timestamp, row2.bi)

    val statement3 = PrimitivesJoda.insert
      .value(_.pkey, row3.pkey)
      .value(_.intColumn, row3.int)
      .value(_.timestamp, row3.bi)

    val batch = Batch.logged.add(statement1).add(statement2).add(statement3)

    val chain = for {
      ex <- PrimitivesJoda.truncate.execute()
      batchDone <- batch.execute()
      count <- PrimitivesJoda.select.count.get()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 3
      }
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.select.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 1
      }
    }
  }

  it should "correctly execute a chain of INSERT queries and not perform multiple inserts with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged.add(statement1).add(statement1.ifNotExists()).add(statement1.ifNotExists())

    val chain = for {
      ex <- PrimitivesJoda.truncate.future()
      batchDone <- batch.future()
      count <- PrimitivesJoda.select.count.one()
    } yield count

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual 1
      }
    }
  }

  it should "correctly execute an UPDATE/DELETE pair batch query" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

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
      .and(_.timestamp setTo  row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.future()
      s3 <- statement2.future()
      b <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
      deleted <- PrimitivesJoda.select.where(_.pkey eqs row3.pkey).one()
    } yield (updated, deleted)

    w successful {
      res => {
        res._1.isDefined shouldEqual true
        res._1.get shouldEqual row2

        res._2.isEmpty shouldEqual true
      }
    }
  }

  it should "correctly execute a batch query with Twitter Futures" in {
    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

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
      .and(_.timestamp setTo row2.bi)

    val statement4 = PrimitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)

    val w = for {
      s1 <- statement1.execute()
      s3 <- statement2.execute()
      b <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
      deleted <- PrimitivesJoda.select.where(_.pkey eqs row3.pkey).get()
    } yield (updated, deleted)

    w successful {
      res => {
        res._1.isDefined shouldEqual true
        res._1.get shouldEqual row2

        res._2.isEmpty shouldEqual true
      }
    }
  }

  ignore should "prioritise batch updates in a last first order" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 20)
      }
    }
  }

  ignore should "prioritise batch updates in a last first order with Twitter Futures" in {
    val row = gen[JodaRow]

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1)
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo row.int))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15)))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 20)))

    val chain = for {
      done <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 20)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp" in {
    val row = gen[JodaRow]

    val last = new DateTime()
    val last1 = last.withDurationAdded(100, 5)
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1.timestamp(last.getMillis))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last1.getMillis))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.future()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).one()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 15)
      }
    }
  }

  ignore should "prioritise batch updates based on a timestamp with Twitter futures" in {
    val row = gen[JodaRow]

    val last = new DateTime()
    val last1 = last.withDurationAdded(100, 5)
    val last2 = last.withDurationAdded(1000, 5)

    val statement1 = PrimitivesJoda.insert
      .value(_.pkey, row.pkey)
      .value(_.intColumn, row.int)
      .value(_.timestamp, row.bi)

    val batch = Batch.logged
      .add(statement1.timestamp(last))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 10)).timestamp(last1.getMillis))
      .add(PrimitivesJoda.update.where(_.pkey eqs row.pkey).modify(_.intColumn setTo (row.int + 15))).timestamp(last2.getMillis)

    val chain = for {
      done <- batch.execute()
      updated <- PrimitivesJoda.select.where(_.pkey eqs row.pkey).get()
    } yield updated

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.int shouldEqual (row.int + 15)
      }
    }
  }
}
