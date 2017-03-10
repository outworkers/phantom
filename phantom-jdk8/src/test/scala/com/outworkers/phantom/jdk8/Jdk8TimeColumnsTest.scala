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
package com.outworkers.phantom.jdk8

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.jdk8.tables.{Jdk8Row, Jdk8Database, _}
import com.outworkers.util.samplers._

class Jdk8TimeColumnsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    if (session.v4orNewer) {
      Jdk8Database.create()
    }
  }

  if (session.v4orNewer) {
    it should "correctly insert and extract java.time columns" in {
      val row = gen[Jdk8Row]

      val chain = for {
        store <- Jdk8Database.primitivesJdk8.store(row).future()
        select <- Jdk8Database.primitivesJdk8.select.where(_.pkey eqs row.pkey).one()
      } yield select

      whenReady(chain) { res =>
        res.value shouldEqual row
      }
    }

    it should "correctly insert and extract optional java.time columns" in {
      val row = gen[OptionalJdk8Row]

      val chain = for {
        store <- Jdk8Database.optionalPrimitivesJdk8.store(row).future()
        select <- Jdk8Database.optionalPrimitivesJdk8.select.where(_.pkey eqs row.pkey).one()
      } yield select

      whenReady(chain) { res =>
        res.value shouldEqual row
      }
    }
  }
}
