package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.exceptions.{SyntaxError, InvalidQueryException}

import scala.concurrent.Await
import scala.concurrent.duration._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{TestRow, IndexedCollectionsTable}
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class IndexedCollectionsTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(IndexedCollectionsTable.create.ifNotExists().future(), 4.seconds)
  }

  it should "store a record and retrieve it with a CONTAINS query on the SET" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).future()
      get <- IndexedCollectionsTable.select.where(_.setText contains record.setText.head).fetch()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }

  }

  it should "store a record and retrieve it with a CONTAINS query on the SET with TWitter Futures" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).execute()
      get <- IndexedCollectionsTable.select.where(_.setText contains record.setText.head).collect()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).future()
      get <- IndexedCollectionsTable.select.where(_.mapTextToText contains record.mapTextToText.head._2).fetch()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS query on the MAP with Twitter Futures" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).execute()
      get <- IndexedCollectionsTable.select.where(_.mapTextToText contains record.mapTextToText.head._2).collect()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS KEY query on the MAP" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).future()
      get <- IndexedCollectionsTable.select.where(_.mapIntToText containsKey record.mapIntToText.head._1).fetch()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

  it should "store a record and retrieve it with a CONTAINS KEY query on the MAP with Twitter Futures" in {
    val record = gen[TestRow]

    val chain = for {
      store <- IndexedCollectionsTable.store(record).execute()
      get <- IndexedCollectionsTable.select.where(_.mapIntToText containsKey record.mapIntToText.head._1).collect()
    } yield get

    if (cassandraVersion > Version.`2.1.0`) {
      chain.successful {
        res => {
          res.nonEmpty shouldEqual true

          res contains record shouldEqual true
        }
      }
    } else {
      chain.failing[SyntaxError]
    }
  }

}
