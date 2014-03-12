package com.newzly.phantom.dsl.crud

import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._
import com.newzly.phantom.Implicits._
import com.newzly.phantom.helper.BaseTest
import com.newzly.phantom.tables.{ Primitive, Primitives }
import com.newzly.util.finagle.AsyncAssertionsHelper._

class SelectTest extends BaseTest {
  implicit val s: PatienceConfiguration.Timeout = timeout(10 seconds)
  val keySpace: String = "selectTest"

  "Select" should "work fine" in {
    val row = Primitive.sample
    Primitives.insertSchema()
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
        .value(_.bi, row.bi).future() flatMap {
        _ => {
          for {
            a <- Primitives.select.fetch
            b <- Primitives.select.where(_.pkey eqs row.pkey).one
          } yield (a contains row, b.get === row)

        }
      }

    rcp successful {
      r => {
        assert(r._1)
        assert(r._2)
      }
    }
  }

  "Selecting 2 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
         Primitives.select(_.pkey, _.long).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

  "Selecting 3 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

  "Selecting 4 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean, _.bDecimal).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }


  "Selecting 5 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

  "Selecting 6 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

  "Selecting 7 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

  "Selecting 8 columns" should "work fine" in {
    val row = Primitive.sample
    val expected = (row.pkey, row.long, row.boolean, row.bDecimal, row.double, row.float, row.inet, row.int)
    Primitives.insertSchema()
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
      .value(_.bi, row.bi).future() flatMap {
      _ => {
        Primitives.select(_.pkey, _.long, _.boolean, _.bDecimal, _.double, _.float, _.inet, _.int).where(_.pkey eqs row.pkey).one()
      }
    }

    rcp successful {
      r => {
        r.isDefined shouldBe true
        r.get shouldBe expected
      }
    }
  }

}
