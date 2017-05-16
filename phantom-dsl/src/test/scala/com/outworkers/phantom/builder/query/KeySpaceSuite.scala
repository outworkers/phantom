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
package com.outworkers.phantom.builder.query

import com.outworkers.phantom.PhantomBaseSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers.Sample
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{FreeSpec, Matchers, Suite}

trait KeySpaceSuite { self: Suite =>

  implicit val keySpace = KeySpace("phantom")
}

trait SerializationTest extends Matchers with TestDatabase.connector.Connector {
  self: Suite =>

  def db: TestDatabase = TestDatabase

  implicit object JodaTimeSampler extends Sample[DateTime] {
    override def sample: DateTime = DateTime.now(DateTimeZone.UTC)
  }
}

trait QueryBuilderTest extends FreeSpec with PhantomBaseSuite with TestDatabase.connector.Connector