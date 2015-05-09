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
package com.websudos.phantom.testkit.suites

import scala.concurrent.duration._

import org.scalatest.concurrent.{PatienceConfiguration, AsyncAssertions, ScalaFutures}
import org.scalatest.{Assertions, BeforeAndAfterAll, FeatureSpec, FlatSpec, Matchers, Suite}

import com.websudos.phantom.connectors.{KeySpace, SimpleCassandraConnector}

trait SimpleCassandraTest extends ScalaFutures
  with SimpleCassandraConnector
  with Matchers
  with Assertions
  with AsyncAssertions
  with BeforeAndAfterAll
  with CassandraSetup {
  self : BeforeAndAfterAll with Suite =>

  /**
   * The default timeout value for phantom tests, passed implicitly to the testing framework.
   * @return The default timeout value.
   */
  implicit def patience: PatienceConfiguration.Timeout = timeout(5 seconds)

  override def beforeAll() {
    super.beforeAll()
    setupCassandra()
    manager.initIfNotInited(keySpace.name)
  }
}

trait CassandraFlatSpec extends FlatSpec with SimpleCassandraTest
trait CassandraFeatureSpec extends FeatureSpec with SimpleCassandraTest

trait PhantomCassandraConnector extends SimpleCassandraConnector {
  implicit val keySpace = KeySpace("phantom")
}

trait PhantomCassandraTestSuite extends CassandraFlatSpec with PhantomCassandraConnector
