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
package com.websudos.phantom.reactivestreams.suites

import akka.actor.ActorSystem
import com.websudos.phantom.builder.query.{ExecutableStatement, Batchable}
import com.websudos.phantom.dsl._
import com.websudos.phantom.reactivestreams.RequestBuilder
import com.websudos.phantom.testkit.suites.{PhantomCassandraConnector, PhantomCassandraTestSuite}
import com.websudos.util.testing._
import org.scalatest.Suite

trait TestImplicits {
  implicit val system = ActorSystem()

  implicit object OperaRequestBuilder extends RequestBuilder[OperaTable, Opera] {

    override def request(ct: OperaTable, t: Opera)(implicit session: Session, keySpace: KeySpace): ExecutableStatement with Batchable = {
      ct.insert.value(_.name, t.name)
    }
  }

}

trait StreamTest extends PhantomCassandraTestSuite with TestImplicits {
  self: Suite =>

  override def beforeAll() {
    super.beforeAll()
    OperaTable.insertSchema()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }

}

case class Opera(name: String)

abstract class OperaTable extends CassandraTable[OperaTable, Opera] with PhantomCassandraConnector {
  object name extends StringColumn(this) with PartitionKey[String]

  def fromRow(row: Row): Opera = {
    Opera(name(row))
  }
}

object OperaTable extends OperaTable with PhantomCassandraConnector

object OperaData {
  val operas = genList[String]().map(Opera)
}