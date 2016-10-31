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
package com.outworkers.phantom

import java.util.concurrent.TimeUnit

import com.datastax.driver.core.VersionNumber
import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.util.lift.{DateTimeSerializer, UUIDSerializer}
import com.outworkers.phantom.tables.TestDatabase
import org.scalatest._
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest.time.{Millis, Seconds, Span}

trait PhantomBaseSuite extends Suite with Matchers
  with BeforeAndAfterAll
  with RootConnector
  with ScalaFutures
  with OptionValues {

  protected[this] val defaultScalaTimeoutSeconds = 25

  private[this] val defaultScalaInterval = 50L

  implicit val formats = net.liftweb.json.DefaultFormats + new UUIDSerializer + new DateTimeSerializer

  implicit val defaultScalaTimeout = scala.concurrent.duration.Duration(defaultScalaTimeoutSeconds, TimeUnit.SECONDS)

  private[this] val defaultTimeoutSpan = Span(defaultScalaTimeoutSeconds, Seconds)

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(defaultTimeoutSpan)

  override implicit val patienceConfig = PatienceConfig(
    timeout = defaultTimeoutSpan,
    interval = Span(defaultScalaInterval, Millis)
  )

  implicit class CqlConverter[T](val obj: T) {
    def asCql()(implicit primitive: com.outworkers.phantom.builder.primitives.Primitive[T]): String = {
      primitive.asCql(obj)
    }
  }
}

trait PhantomSuite extends FlatSpec with PhantomBaseSuite with TestDatabase.connector.Connector {
  val database = TestDatabase

  def requireVersion[T](v: VersionNumber)(fn: => T): Unit = if (cassandraVersion.value.compareTo(v) >= 0) fn else ()
}


trait PhantomFreeSuite extends FreeSpec with PhantomBaseSuite with TestDatabase.connector.Connector {
  val database = TestDatabase
}