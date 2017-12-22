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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import com.outworkers.util.samplers._

class TruncateTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.articles.createSchema()
  }

  it should "truncate all records in a table" in {
    val articles = genList[Article]()

    val result = for {
      truncateBefore <- database.articles.truncate.future()
      store <- database.articles.storeRecords(articles)
      records <- database.articles.select.fetch
      truncate <- database.articles.truncate.future()
      records1 <- database.articles.select.fetch
    } yield (records, records1)


    whenReady(result) { case (init, updated) =>
      init should have size articles.size
      info (s"inserted exactly ${init.size} records")

      updated should have size 0
      info (s"got exactly ${updated.size} records")
    }
  }

  it should "allow setting a consistency level on a truncate query" in {
    val articles = genList[Article]()

    val result = for {
      truncateBefore <- database.articles.truncate.future()
      i1 <- database.articles.storeRecords(articles)
      records <- database.articles.select.fetch
      truncate <- database.articles.truncate.consistencyLevel_=(ConsistencyLevel.ONE).future()
      records1 <- database.articles.select.fetch
    } yield (records, records1)


    whenReady(result) { case (init, updated) =>
      init should have size articles.size
      info (s"inserted exactly ${init.size} records")

      updated should have size 0
      info (s"got exactly ${updated.size} records")
    }
  }
}
