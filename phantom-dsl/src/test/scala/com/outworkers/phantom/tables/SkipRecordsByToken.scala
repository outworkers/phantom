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
package com.outworkers.phantom.tables

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.util.samplers._

class SkipRecordsByToken extends PhantomSuite {

  val Articles = database.articles

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = Articles.createSchema()
  }

  it should "allow skipping records using gtToken" in {
    val articles = genList[Article]()

    val result = for {
      _ <- Articles.truncate().future()
      _ <- Articles.storeRecords(articles)
      one <- Articles.select.one
      next <- Articles.select.where(_.id gtToken one.value.id).fetch
    } yield next

    whenReady(result) { r =>
      info (s"got exactly ${r.size} records")
      r.size shouldEqual (articles.size - 1)
    }
  }

  it should "allow skipping records using ltToken" in {
    val articles = genList[Article]()

    val result = for {
      _ <- Articles.truncate().future()
      _ <- Articles.storeRecords(articles)
      list <- Articles.select.fetch
      next <- Articles.select.where(_.id ltToken list.last.id).fetch
    } yield next

    whenReady(result) { r =>
      info (s"got exactly ${r.size} records")
      r.size shouldEqual (articles.size - 1)
    }
  }

  it should "allow skipping records using lteToken" in {
    val articles = genList[Article]()

    val result = for {
      _ <- Articles.truncate().future()
      _ <- Articles.storeRecords(articles)
      list <- Articles.select.fetch
      next <- Articles.select.where(_.id lteToken list.last.id).fetch
    } yield next

    whenReady(result) { r =>
      info (s"got exactly ${r.size} records")
      r.size shouldEqual articles.size
    }
  }

  it should "allow skipping records using eqsToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Articles.storeRecords(articles)
      next <- Articles.select.where(_.id eqsToken articles.headOption.value.id).fetch
    } yield next

    whenReady(result) { r =>
      info (s"got exactly ${r.size} records")
      r.size shouldEqual 1
    }
  }

  it should "allow skipping records using gteToken" in {
    val articles = genList[Article]()

    val result = for {
      truncate <- Articles.truncate.future()
      store <- Articles.storeRecords(articles)
      list <- Articles.select.fetch
      next <- Articles.select.where(_.id gteToken list.headOption.value.id).fetch
    } yield next


    whenReady(result) { r =>
      info (s"got exactly ${r.size} records")
      r.size shouldEqual articles.size
    }
  }

}
