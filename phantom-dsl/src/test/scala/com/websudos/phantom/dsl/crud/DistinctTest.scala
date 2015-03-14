package com.websudos.phantom.dsl.crud

import java.util.UUID

import com.websudos.phantom.Implicits._
import com.websudos.phantom.batch.BatchStatement
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._
import org.joda.time.DateTime
import org.scalatest.concurrent.PatienceConfiguration
import org.scalatest.time.SpanSugar._

class DistinctTest extends PhantomCassandraTestSuite {
    implicit val s: PatienceConfiguration.Timeout = timeout(5 seconds)

    override def beforeAll(): Unit = {
        super.beforeAll()
        TableWithCompoundKey.insertSchema()
    }

    it should "return distinct primary keys" in {
        val rows = List(
            StubRecord("a", UUID.nameUUIDFromBytes("1".getBytes)),
            StubRecord("b", UUID.nameUUIDFromBytes("1".getBytes)),
            StubRecord("c", UUID.nameUUIDFromBytes("2".getBytes)),
            StubRecord("d", UUID.nameUUIDFromBytes("3".getBytes))
        )

        val batch = rows.foldLeft(BatchStatement())((batch, row) => {
            batch.add(
                TableWithCompoundKey.insert
                    .value(_.id, row.id)
                    .value(_.second, UUID.nameUUIDFromBytes(row.name.getBytes))
                    .value(_.name, row.name)
            )
        })

        val chain = for {
            truncate <- TableWithCompoundKey.truncate.future()
            batch <- batch.future()
            list <- TableWithCompoundKey.distinct(_.id).fetch
        } yield list

        val expectedResult = rows.filter(_.name != "b").map(_.id)
        chain successful {
            res => {
                res should contain only (expectedResult: _*)
            }
        }
    }

    private implicit def string2date(date: String): DateTime = new DateTime(date)
}
