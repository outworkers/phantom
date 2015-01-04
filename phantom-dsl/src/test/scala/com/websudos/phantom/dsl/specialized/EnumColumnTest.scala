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

import com.datastax.driver.core.utils.UUIDs
import com.twitter.conversions.time._
import com.twitter.util.Await
import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraTestSuite
import com.websudos.phantom.tables.{EnumRecord, EnumTable, Records}
import com.websudos.util.testing._

class EnumColumnTest extends PhantomCassandraTestSuite {
  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.result(EnumTable.create.execute(), 2.seconds)
  }

  it should "store a simple record and parse an Enumeration value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, None)


    val chain = for {
      insert <- EnumTable.insert.value(_.id, sample.name).value(_.enum, sample.enum).value(_.optEnum, sample.optEnum).execute()
      get <- EnumTable.select.where(_.id eqs sample.name).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.enum shouldEqual sample.enum
        res.get.optEnum.isDefined shouldEqual false
        res.get.optEnum shouldEqual None
      }
    }
  }

  it should "store a simple record and parse an Enumeration value and an Optional value back from the stored value" in {
    val sample = EnumRecord(UUIDs.timeBased().toString, Records.TypeOne, Some(Records.TypeTwo))


    val chain = for {
      insert <- EnumTable.insert.value(_.id, sample.name).value(_.enum, sample.enum).value(_.optEnum, sample.optEnum).execute()
      get <- EnumTable.select.where(_.id eqs sample.name).get()
    } yield get

    chain.successful {
      res => {
        res.isDefined shouldEqual true
        res.get.enum shouldEqual sample.enum
        res.get.optEnum.isDefined shouldEqual true
        res.get.optEnum shouldEqual sample.optEnum
      }
    }
  }
}
