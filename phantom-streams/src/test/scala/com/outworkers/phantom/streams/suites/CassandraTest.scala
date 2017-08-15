/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.streams.suites

import akka.actor.ActorSystem
import com.outworkers.phantom.builder.query.Batchable
import com.outworkers.phantom.connectors.CassandraConnection
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.streams.RequestBuilder
import com.outworkers.util.samplers._
import org.scalatest._

trait TestImplicits {
  implicit val system = ActorSystem()

  implicit object OperaRequestBuilder extends RequestBuilder[OperaTable, Opera] {

    override def request(ct: OperaTable, t: Opera)(
      implicit session: Session,
      keySpace: KeySpace
    ): Batchable = ct.insert.value(_.name, t.name)
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
    StreamDatabase.create()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    // Note we are intentionally using the deprecated API here to make sure we do not break Scala 2.10
    // compatibility. Please do not use [[system.terminate()]] just yet.
    system.shutdown()
  }

}

case class Opera(name: String)

abstract class OperaTable extends Table[OperaTable, Opera] {
  object name extends StringColumn with PartitionKey
}

object StreamConnector {
  val connector: CassandraConnection = ContactPoint.local.keySpace("phantom")
}

class StreamDatabase extends Database[StreamDatabase](StreamConnector.connector) {
  object operaTable extends OperaTable with connector.Connector
}

object StreamDatabase extends StreamDatabase

object OperaData {
  val operas: List[Opera] = genList[String]().map(Opera)
}
