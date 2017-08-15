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
package com.outworkers.phantom.builder.query.db.specialized

import com.datastax.driver.core.utils.UUIDs
import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables._
import scala.concurrent.Await

class EnumColumnTest extends PhantomSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(database.enumTable.create.ifNotExists().future(), defaultScalaTimeout)
    Await.result(database.namedEnumTable.create.ifNotExists().future(), defaultScalaTimeout)
    Await.result(database.indexedEnumTable.create.ifNotExists().future(), defaultScalaTimeout)
  }

  it should "store a simple record and parse an Enumeration value back from the stored value" in {
    val sample = EnumRecord(
      UUIDs.timeBased().toString,
      Records.TypeOne,
      None,
      SingletonEnum.One
    )

    val chain = for {
      _ <- database.enumTable.store(sample).future()
      rec <- database.enumTable.select.where(_.id eqs sample.name).one()
    } yield rec

    whenReady(chain) { res =>
      res.value.enum shouldEqual sample.enum
      res.value.optEnum shouldBe empty
    }
  }

  it should "store a simple record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = EnumRecord(
      name = UUIDs.timeBased().toString,
      enum = Records.TypeOne,
      optEnum = Some(Records.TypeTwo),
      singleton = SingletonEnum.One
    )

    val chain = for {
      _ <- database.enumTable.store(sample).future()
      rec <- database.enumTable.select.where(_.id eqs sample.name).one()
    } yield rec

    whenReady(chain) { res =>
      res.value.enum shouldEqual sample.enum
      res.value.optEnum shouldBe defined
      res.value.optEnum shouldEqual sample.optEnum
    }
  }

  it should "store a named record and parse an Enumeration value back from the stored value" in {
    val sample = NamedEnumRecord(
      UUIDs.timeBased().toString,
      NamedRecords.One,
      None
    )

    val chain = for {
      _ <- database.namedEnumTable.store(sample).future()
      rec <- database.namedEnumTable.select.where(_.id eqs sample.name).one()
    } yield rec

    whenReady(chain) { res =>
      res.value.enum shouldEqual sample.enum
      res.value.optEnum shouldBe empty
    }
  }

  it should "store a named record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = NamedEnumRecord(
      UUIDs.timeBased().toString,
      NamedRecords.One,
      Some(NamedRecords.One)
    )

    val chain = for {
      _ <- database.namedEnumTable.store(sample).future()
      get <- database.namedEnumTable.select.where(_.id eqs sample.name).one()
    } yield get

    whenReady(chain) { res =>
      res.value.enum shouldEqual sample.enum
      res.value.optEnum shouldBe defined
      res.value.optEnum shouldEqual sample.optEnum
    }
  }

  it should "store and retrieve an indexed enumeration record" in {
    val sample = NamedPartitionRecord(
      NamedRecords.One,
      UUIDs.random()
    )

    val chain = for {
      _ <- database.indexedEnumTable.truncate().future()
      _ <- database.indexedEnumTable.store(sample).future()
      get <- database.indexedEnumTable.select.where(_.enum eqs NamedRecords.One).one()
    } yield get

    whenReady(chain) { res =>
      res.value shouldEqual sample
    }
  }

}


