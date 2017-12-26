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
package com.outworkers.phantom.builder.query.bugs

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.tables.bugs.{SchemaBug663A, SchemaBug663B}
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._

class SchemaBug663Test extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    val _ = database.schemaBug663Table.createSchema()
  }

  it should "automatically extract an A record if the discriminator is an even number" in {
    val sample = gen[SchemaBug663A]
    val discriminator = 2

    val chain = for {
      _ <- database.schemaBug663Table.insert
        .value(_.discriminator, discriminator)
        .value(_.a, sample.a)
        .future()
      one <- database.schemaBug663Table.select.where(_.discriminator eqs discriminator).one()
    } yield one

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldBe a [SchemaBug663A]
    }
  }

  it should "automatically extract an A record if the discriminator is an odd number" in {
    val sample = gen[SchemaBug663B]
    val discriminator = 3

    val chain = for {
      _ <- database.schemaBug663Table.insert
        .value(_.discriminator, discriminator)
        .value(_.b, sample.b)
        .future()
      one <- database.schemaBug663Table.select.where(_.discriminator eqs discriminator).one()
    } yield one

    whenReady(chain) { res =>
      res shouldBe defined
      res.value shouldBe a [SchemaBug663B]
    }
  }
}
