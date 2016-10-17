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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.outworkers.phantom.reactivestreams.suites

import akka.actor.ActorSystem
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.reactivestreams.RequestBuilder
import com.outworkers.phantom.builder.query.{Batchable, ExecutableStatement, InsertQuery}
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.util.testing._
import org.scalatest._

import scala.concurrent.Await
import scala.concurrent.duration._

trait TestImplicits {
  implicit val system = ActorSystem()

  implicit object OperaRequestBuilder extends RequestBuilder[ConcreteOperaTable, Opera] {

    override def request(ct: ConcreteOperaTable, t: Opera)(
      implicit session: Session,
      keySpace: KeySpace
    ): ExecutableStatement with Batchable = {
      ct.insert.value(_.name, t.name)
    }
  }

}

trait StreamTest extends FlatSpec with BeforeAndAfterAll
  with OptionValues
  with Matchers
  with TestImplicits
  with Retries
  with StreamDatabase.connector.Connector {
  self: Suite =>

  override def withFixture(test: NoArgTest): Outcome = {
    if (isRetryable(test)) {
      withRetry(super.withFixture(test))
    } else {
      super.withFixture(test)
    }
  }

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(StreamDatabase.autocreate().future(), 5.seconds)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.shutdown()
  }

}

case class Opera(name: String)

abstract class OperaTable extends CassandraTable[ConcreteOperaTable, Opera] {
  object name extends StringColumn(this) with PartitionKey[String]

  def fromRow(row: Row): Opera = {
    Opera(name(row))
  }
}

abstract class ConcreteOperaTable extends OperaTable with RootConnector {
  def store(item: Opera): InsertQuery.Default[ConcreteOperaTable, Opera] = {
    insert.value(_.name, item.name)
  }
}


object StreamConnector {
  val connector = ContactPoint.local.keySpace("phantom")
}

class StreamDatabase extends Database[StreamDatabase](StreamConnector.connector) {
  object operaTable extends ConcreteOperaTable with connector.Connector
}

object StreamDatabase extends StreamDatabase

object OperaData {
  val operas = genList[String]().map(Opera)
}