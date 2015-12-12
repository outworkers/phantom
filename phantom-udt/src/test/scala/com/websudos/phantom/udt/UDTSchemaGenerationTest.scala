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
package com.websudos.phantom.udt

import com.datastax.driver.core.exceptions.SyntaxError
import com.websudos.phantom.dsl._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

import scala.concurrent.Await
import scala.concurrent.duration._


class UDTSchemaGenerationTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()

    if (cassandraVersion > Version.`2.1.0`) {
      Await.result(TestFields.udt.future(), 5.seconds)
    }
  }

  it should "generate the schema of an UDT during table creation" in {
    info(UDTCollector.statements.list.map(_.queryString).mkString("\n"))

    if (cassandraVersion > Version.`2.1.0`) {
      whenReady(TestFields.udt.future()) {
        res => {
          res.isEmpty shouldEqual false
          res.headOption.value.wasApplied() shouldEqual true
        }
      }
    } else {
      TestFields.udt.future().failing[SyntaxError]
    }

  }
}
