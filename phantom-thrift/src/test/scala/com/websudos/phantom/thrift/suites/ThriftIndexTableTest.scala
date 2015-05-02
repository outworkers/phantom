package com.websudos.phantom.thrift.suites

import com.twitter.scrooge.CompactThriftSerializer
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{ThriftColumnTable, Output, ThriftIndexedTable}
import com.websudos.phantom.testkit.PhantomCassandraTestSuite
import com.websudos.phantom.thrift._
import com.websudos.util.testing._

class ThriftIndexTableTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    ThriftIndexedTable.insertSchema()
  }

  implicit object SamplePrimitve extends ThriftPrimitive[ThriftTest] {
    val serializer = CompactThriftSerializer(ThriftTest)
  }

  it should "allow storing a thrift class inside a table indexed by a thrift struct" in {
    val sample = gen[Output]

    val chain = for {
      store <- ThriftIndexedTable.store(sample).future()
      get <- ThriftIndexedTable.select.where(_.ref eqs sample.struct).one()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true

        res.get.id shouldEqual sample.id
        res.get.name shouldEqual sample.name
        res.get.struct shouldEqual sample.struct
        res.get.optThrift shouldEqual sample.optThrift
        res.get.thriftList shouldEqual sample.thriftList
        res.get.thriftMap shouldEqual sample.thriftMap
        res.get.thriftSet shouldEqual sample.thriftSet

        res.get shouldEqual sample
      }
    }
  }

  it should "allow storing a thrift class inside a table indexed by a thrift struct with Twitter futures" in {
    val sample = gen[Output]

    val chain = for {
      store <- ThriftIndexedTable.store(sample).execute()
      get <- ThriftIndexedTable.select.where(_.ref eqs sample.struct).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true

        res.get.id shouldEqual sample.id
        res.get.name shouldEqual sample.name
        res.get.struct shouldEqual sample.struct
        res.get.optThrift shouldEqual sample.optThrift
        res.get.thriftList shouldEqual sample.thriftList
        res.get.thriftMap shouldEqual sample.thriftMap
        res.get.thriftSet shouldEqual sample.thriftSet

        res.get shouldEqual sample
      }
    }
  }
}
