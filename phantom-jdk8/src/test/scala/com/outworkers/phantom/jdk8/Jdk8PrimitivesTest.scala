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
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Matchers, OptionValues}
import com.outworkers.phantom.jdk8.tables._

class Jdk8PrimitivesTest extends PhantomSuite with Matchers with OptionValues with GeneratorDrivenPropertyChecks {

  override def beforeAll(): Unit = {
    super.beforeAll()
    Jdk8Database.create()
  }

  it should "correctly convert a ZonedDateTime to a tuple and back" in {
    forAll { row: Jdk8Row =>
      val chain = for {
        store <- Jdk8Database.primitivesJdk8.store(row).future()
        one <- Jdk8Database.primitivesJdk8.findByPkey(row.pkey)
      } yield one

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual row
      }
    }
  }

  it should "correctly convert a optional datetime types to Cassandra and back" in {
    forAll { row: OptionalJdk8Row =>
      val chain = for {
        store <- Jdk8Database.optionalPrimitivesJdk8.store(row).future()
        one <- Jdk8Database.optionalPrimitivesJdk8.findByPkey(row.pkey)
      } yield one

      whenReady(chain) { res =>
        res shouldBe defined
        res.value shouldEqual row
      }
    }
  }
}