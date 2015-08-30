package com.websudos.phantom.thrift.suites

import com.twitter.scrooge.CompactThriftSerializer
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{Output, ThriftIndexedTable}
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
        res.value.id shouldEqual sample.id
        res.value.name shouldEqual sample.name
        res.value.struct shouldEqual sample.struct
        res.value.optThrift shouldEqual sample.optThrift
        res.value.thriftList shouldEqual sample.thriftList
        res.value.thriftMap shouldEqual sample.thriftMap
        res.value.thriftSet shouldEqual sample.thriftSet

        res.value shouldEqual sample
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
        res.value.id shouldEqual sample.id
        res.value.name shouldEqual sample.name
        res.value.struct shouldEqual sample.struct
        res.value.optThrift shouldEqual sample.optThrift
        res.value.thriftList shouldEqual sample.thriftList
        res.value.thriftMap shouldEqual sample.thriftMap
        res.value.thriftSet shouldEqual sample.thriftSet

        res.value shouldEqual sample
      }
    }
  }
}
