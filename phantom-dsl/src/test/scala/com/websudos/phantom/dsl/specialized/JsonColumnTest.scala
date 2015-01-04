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

package com.websudos.phantom.dsl.specialized

import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables._
import com.websudos.phantom.testing.PhantomCassandraTestSuite
import com.websudos.util.testing._

class JsonColumnTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    JsonTable.insertSchema()
  }

  it should "allow storing a JSON record" in {
    val sample = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .future()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).one
    } yield select

    chain.successful {
      res => {
        res.isEmpty shouldEqual false
        res.get shouldEqual sample
      }
    }
  }

  it should "allow storing a JSON record with Twitter Futures" in {
    val sample = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .execute()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).get
    } yield select

    chain.successful {
      res => {
        res.isEmpty shouldEqual false
        res.get shouldEqual sample
      }
    }
  }

  it should "allow updating a JSON record" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .future()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get.json shouldEqual sample.json

        res._2.isEmpty shouldEqual false
        res._2.get.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .execute()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get.json shouldEqual sample.json

        res._2.isEmpty shouldEqual false
        res._2.get.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .future()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get shouldEqual sample

        res._2.isEmpty shouldEqual false
        res._2.get.jsonList(0) shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .execute()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get shouldEqual sample

        res._2.isEmpty shouldEqual false
        res._2.get.jsonList(0) shouldEqual sample2.json
      }
    }
  }

  ignore should "allow updating a JSON record in a Set of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .future()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get shouldEqual sample

        res._2.isEmpty shouldEqual false
        res._2.get.jsonSet.last shouldEqual sample2.json
      }
    }
  }

  ignore should "allow updating a JSON record in a Set of JSON records with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val insert = JsonTable.insert
      .value(_.id, sample.id)
      .value(_.name, sample.name)
      .value(_.json, sample.json)
      .value(_.jsonList, sample.jsonList)
      .value(_.jsonSet, sample.jsonSet)
      .execute()

    val chain = for {
      done <- insert
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.isEmpty shouldEqual false
        res._1.get shouldEqual sample

        res._2.isEmpty shouldEqual false
        res._2.get.jsonSet.last shouldEqual sample2.json
      }
    }
  }
}
