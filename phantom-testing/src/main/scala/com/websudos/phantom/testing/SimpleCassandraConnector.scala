/*
 *
 *  * Copyright 2014 websudos ltd.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.websudos.phantom.testing

import org.scalatest.concurrent.{AsyncAssertions, ScalaFutures}
import org.scalatest.{Assertions, BeforeAndAfterAll, FeatureSpec, FlatSpec, Matchers, Suite}

import com.websudos.phantom.zookeeper.SimpleCassandraConnector

trait SimpleCassandraTest extends ScalaFutures
  with SimpleCassandraConnector
  with Matchers
  with Assertions
  with AsyncAssertions
  with BeforeAndAfterAll
  with CassandraSetup{
  self : BeforeAndAfterAll with Suite =>

  override def beforeAll() {
    super.beforeAll()
    setupCassandra()
    manager.initIfNotInited(keySpace)
  }
}

trait CassandraFlatSpec extends FlatSpec with SimpleCassandraTest
trait CassandraFeatureSpec extends FeatureSpec with SimpleCassandraTest


trait PhantomCassandraConnector extends SimpleCassandraConnector {
  val keySpace = "phantom"
}

trait PhantomCassandraTestSuite extends CassandraFlatSpec with PhantomCassandraConnector
