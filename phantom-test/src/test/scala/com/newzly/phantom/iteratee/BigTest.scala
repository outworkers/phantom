/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.iteratee

import scala.concurrent.{ blocking, ExecutionContext }
import org.scalatest.{ Assertions, BeforeAndAfterAll, FlatSpec, Matchers  }
import org.scalatest.concurrent.{ AsyncAssertions, ScalaFutures }
import com.datastax.driver.core.Session
import com.newzly.phantom.Manager
import com.twitter.util.Duration

trait BigTest extends FlatSpec with ScalaFutures with BeforeAndAfterAll with Matchers with Assertions with AsyncAssertions {
  val keySpace: String
  val cluster = BigTestHelper.cluster
  implicit lazy val session: Session = cluster.connect()
  implicit lazy val context: ExecutionContext = Manager.scalaExecutor

  private[this] def createKeySpace(spaceName: String) = {
    session.execute(s"CREATE KEYSPACE $spaceName WITH replication = {'class': 'SimpleStrategy', 'replication_factor' : 1};")
    session.execute(s"use $spaceName;")
    cluster.getConfiguration.getSocketOptions.setReadTimeoutMillis(Duration.fromSeconds(3).inSeconds)
  }

  override def beforeAll() {
    session.execute(s"DROP KEYSPACE $keySpace;")
    createKeySpace(keySpace)
  }

  override def afterAll() {
    blocking {
      session.execute(s"DROP KEYSPACE $keySpace;")
    }
  }

}
