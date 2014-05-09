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
package com.newzly.phantom.dsl.query

import scala.concurrent.blocking
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ TimeSeriesTable, TimeSeriesRecord }
import com.newzly.util.testing.AsyncAssertionsHelper._
import com.newzly.util.testing.Sampler
import com.newzly.util.testing.cassandra.BaseTest

class OrderByQueries extends BaseTest {
  val keySpace = "order_by_test"

  override def beforeAll(): Unit = {
    blocking {
      super.beforeAll()
      TimeSeriesTable.insertSchema()
    }
  }

}

