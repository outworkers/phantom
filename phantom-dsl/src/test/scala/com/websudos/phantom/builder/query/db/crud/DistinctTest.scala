package com.websudos.phantom.builder.query.db.crud

import java.util.UUID
import org.joda.time.DateTime
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit.suites.PhantomCassandraTestSuite
import com.websudos.util.testing._

class DistinctTest extends PhantomCassandraTestSuite {
  implicit val s: PatienceConfiguration.Timeout = timeout(5 seconds)

  override def beforeAll(): Unit = {
      super.beforeAll()
      TestDatabase.tableWithCompoundKey.insertSchema()
  }

  it should "return distinct primary keys" in {
    val rows = List(
      StubRecord("a", UUID.nameUUIDFromBytes("1".getBytes)),
      StubRecord("b", UUID.nameUUIDFromBytes("1".getBytes)),
      StubRecord("c", UUID.nameUUIDFromBytes("2".getBytes)),
      StubRecord("d", UUID.nameUUIDFromBytes("3".getBytes))
    )

    val batch = rows.foldLeft(Batch.unlogged)((batch, row) => {
      batch.add(
        TestDatabase.tableWithCompoundKey.insert
          .value(_.id, row.id)
          .value(_.second, UUID.nameUUIDFromBytes(row.name.getBytes))
          .value(_.name, row.name)
      )
    })

    val chain = for {
      truncate <- TestDatabase.tableWithCompoundKey.truncate.future()
      batch <- batch.future()
      list <- TestDatabase.tableWithCompoundKey.select(_.id).distinct.fetch
    } yield list

    val expectedResult = rows.filter(_.name != "b").map(_.id)
    chain successful {
      res => {
        res should contain only (expectedResult: _*)
      }
    }
  }

  private[this] implicit def string2date(date: String): DateTime = new DateTime(date)
}
