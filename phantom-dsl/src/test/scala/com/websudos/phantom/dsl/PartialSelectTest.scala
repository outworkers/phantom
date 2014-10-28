package com.websudos.phantom.dsl

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.Implicits._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.phantom.tables._
import com.websudos.util.testing._


class PartialSelectTest extends PhantomCassandraTestSuite {

  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Primitives.insertSchema()
  }

  "Partially selecting 2 fields" should "correctly select the fields" in {
    val row = gen[Primitive]
    val insert =  Primitives.insert
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

    val chain = for {
      truncate <- Primitives.truncate.future()
      insertDone <- insert.future()
      listSelect <- Primitives.select(_.pkey).fetch
      oneSelect <- Primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).one
    } yield (listSelect, oneSelect)

    chain successful {
      result => {
        result._1 shouldEqual List(row.pkey)
        result._2 shouldEqual Some(Tuple2(row.long, row.boolean))
      }
    }
  }

  "Partially selecting 2 fields" should "work fine with Twitter Futures" in {
    val row = gen[Primitive]
    val insert =  Primitives.insert
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

    val chain = for {
      truncate <- Primitives.truncate.execute()
      insertDone <- insert.execute()
      listSelect <- Primitives.select(_.pkey).collect
      oneSelect <- Primitives.select(_.long, _.boolean).where(_.pkey eqs row.pkey).get
    } yield (listSelect, oneSelect)

    chain successful {
      result => {
        result._1.toList shouldEqual List(row.pkey)
        result._2 shouldEqual Some(Tuple2(row.long, row.boolean))
      }
    }
  }
}
