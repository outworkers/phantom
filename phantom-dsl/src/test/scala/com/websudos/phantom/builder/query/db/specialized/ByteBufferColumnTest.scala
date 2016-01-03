/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query.db.specialized

import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.UUID

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{BufferRecord, TestDatabase}
import com.websudos.phantom.testkit._
import com.websudos.phantom.util.ByteString
import com.websudos.util.testing._

class ByteBufferColumnTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    TestDatabase.byteBufferTable.insertSchema()
  }

  it should "store a ByteBuffer string in the database and retrieve it unaltered" in {
    val buffer = BufferRecord(
      gen[UUID],
      ByteBuffer.wrap(gen[String].getBytes),
      ByteString(gen[ShortString].value.getBytes(StandardCharsets.UTF_8))
    )

    val chain = for {
      store <- TestDatabase.byteBufferTable.store(buffer).future()
      get <- TestDatabase.byteBufferTable.getById(buffer.id)
    } yield get

    chain.successful {
      res => {
        res.value shouldEqual buffer
      }
    }
  }
}
