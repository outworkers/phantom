package com.newzly.phantom.dsl.specialized

import scala.concurrent.blocking
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ SecondaryIndexTable, SecondaryIndexRecord }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.cassandra.BaseTest
import com.newzly.util.testing.Sampler
import com.datastax.driver.core.exceptions.InvalidQueryException

class SecondaryIndexTest extends BaseTest {
  val keySpace = "secondary_index_test"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      SecondaryIndexTable.insertSchema()
    }
  }

  it should "allow fetching a record by its secondary index" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .future()
      select <- SecondaryIndexTable.select.where(_.id eqs sample.primary).one
      select2 <- SecondaryIndexTable.select.allowFiltering().where(_.secondary eqs sample.secondary).one()
    } yield (select, select2)

    chain.successful {
      res => {
        val primary = res._1
        val secondary = res._2

        info("Querying by primary key should return the record")
        primary.isDefined shouldBe true
        primary.get shouldEqual sample

        info("Querying by the secondary index key should also return the record")
        secondary.isDefined shouldEqual true
        secondary.get shouldEqual sample
      }
    }
  }

  it should "allow fetching a record by its secondary index with Twitter Futures" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .execute()
      select <- SecondaryIndexTable.select.where(_.id eqs sample.primary).get
      select2 <- SecondaryIndexTable.select.allowFiltering().where(_.secondary eqs sample.secondary).get()
    } yield (select, select2)

    chain.successful {
      res => {
        val primary = res._1
        val secondary = res._2

        info("Querying by primary key should return the record")
        primary.isDefined shouldBe true
        primary.get shouldEqual sample

        info("Querying by the secondary index key should also return the record")
        secondary.isDefined shouldEqual true
        secondary.get shouldEqual sample
      }
    }
  }

  it should "not throw an error if filtering is not enabled when querying by secondary keys" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield select2

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual sample
      }
    }
  }

  it should "not throw an error if filtering is not enabled when querying by secondary keys with Twitter Futures" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield select2

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get shouldEqual sample
      }
    }
  }

  it should "throw an error when updating a record by it's secondary key" in {
    val sample = SecondaryIndexRecord.sample
    val updatedName = Sampler.getARandomString
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      update <- SecondaryIndexTable.update.where(_.secondary eqs sample.secondary).modify(_.name setTo updatedName).future()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

  it should "throw an error when updating a record by it's secondary key with Twitter Futures" in {
    val sample = SecondaryIndexRecord.sample
    val updatedName = Sampler.getARandomString
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
      update <- SecondaryIndexTable.update.where(_.secondary eqs sample.secondary).modify(_.name setTo updatedName).execute()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield (select2, select3)


    chain.failing[InvalidQueryException]
  }

  it should "throw an error when deleting a record by its secondary index" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .future()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
      delete <- SecondaryIndexTable.delete.where(_.secondary eqs sample.secondary).future()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).one()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

  it should "throw an error when deleting a record by its secondary index with Twitter Futures" in {
    val sample = SecondaryIndexRecord.sample
    val chain = for {
      insert <- SecondaryIndexTable.insert
        .value(_.id, sample.primary)
        .value(_.secondary, sample.secondary)
        .value(_.name, sample.name)
        .execute()
      select2 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
      delete <- SecondaryIndexTable.delete.where(_.secondary eqs sample.secondary).execute()
      select3 <- SecondaryIndexTable.select.where(_.secondary eqs sample.secondary).get()
    } yield (select2, select3)

    chain.failing[InvalidQueryException]
  }

}
