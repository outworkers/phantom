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
package com.outworkers.phantom.reactivestreams.suites

import akka.actor.ActorSystem
import com.outworkers.phantom.builder.query.{Batchable, ExecutableStatement, InsertQuery}
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.database.Database
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.reactivestreams.RequestBuilder
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