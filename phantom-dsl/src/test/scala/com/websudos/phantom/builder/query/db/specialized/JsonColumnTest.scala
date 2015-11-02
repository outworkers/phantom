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

package com.websudos.phantom.builder.query.db.specialized

import com.websudos.phantom.dsl._
import com.websudos.phantom.tables._
import com.websudos.phantom.testkit._
import com.websudos.util.testing._

class JsonColumnTest extends PhantomCassandraTestSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    JsonTable.insertSchema()
  }

  it should "allow storing a JSON record" in {
    val sample = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).future()
      select <- JsonTable.select.where(_.id eqs sample.id).one
    } yield select

    chain.successful {
      res => {
        res.value shouldEqual sample
      }
    }
  }

  it should "allow storing a JSON record with Twitter Futures" in {
    val sample = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).execute()
      select <- JsonTable.select.where(_.id eqs sample.id).get
    } yield select

    chain.successful {
      res => {
        res.value shouldEqual sample
      }
    }
  }

  it should "allow updating a JSON record" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).future()
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value.json shouldEqual sample.json
        res._2.value.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).execute()
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.json setTo sample2.json).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value.json shouldEqual sample.json
        res._2.value.json shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).future()
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value shouldEqual sample

        res._2.value.jsonList.headOption.value shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a List of JSON records with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).execute()
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonList setIdx (0, sample2.json) ).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value shouldEqual sample
        res._2.value.jsonList.headOption.value shouldEqual sample2.json
      }
    }
  }

  it should "allow updating a JSON record in a Set of JSON records" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).future()
      select <- JsonTable.select.where(_.id eqs sample.id).one
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).future()
      select2 <- JsonTable.select.where(_.id eqs sample.id).one()
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value shouldEqual sample
        res._2.value.jsonSet should contain (sample2.json)
      }
    }
  }

  it should "allow updating a JSON record in a Set of JSON records with Twitter Futures" in {
    val sample = gen[JsonClass]
    val sample2 = gen[JsonClass]

    val chain = for {
      done <- JsonTable.store(sample).execute()
      select <- JsonTable.select.where(_.id eqs sample.id).get
      update <- JsonTable.update.where(_.id eqs sample.id).modify(_.jsonSet add sample2.json).execute()
      select2 <- JsonTable.select.where(_.id eqs sample.id).get
    } yield (select, select2)

    chain.successful {
      res => {
        res._1.value shouldEqual sample
        res._2.value.jsonSet should contain (sample2.json)
      }
    }
  }
}
