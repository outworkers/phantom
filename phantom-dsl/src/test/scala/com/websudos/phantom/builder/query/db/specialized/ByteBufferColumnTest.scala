package com.websudos.phantom.builder.query.db.specialized

import java.nio.ByteBuffer
import java.util.UUID

import com.websudos.phantom.tables.{ByteBufferTable, BufferRecord}
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class ByteBufferColumnTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    ByteBufferTable.insertSchema()
  }

  it should "store a ByteBuffer string in the database and retrieve it unaltered" in {
    val buffer = BufferRecord(
      gen[UUID],
      ByteBuffer.wrap(gen[String].getBytes)
    )

    val chain = for {
      store <- ByteBufferTable.store(buffer).future()
      get <- ByteBufferTable.getById(buffer.id)
    } yield get

    chain.successful {
      res => {
        res.value shouldEqual buffer
      }
    }
  }
}
