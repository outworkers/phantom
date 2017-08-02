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
package com.outworkers.phantom.builder.query.compilation

import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.phantom.dsl.UUID
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec

class TypeRestrictionsTest extends FlatSpec with SerializationTest {

  val Primitives = TestDatabase.primitives
  val tsTable = TestDatabase.timeSeriesTable


  it should "allow using a correct type for a value method" in {
    "Primitives.insert.value(_.boolean, true)" should compile
  }

  it should "not allow using a wrong type for a value method" in {
    "Primitives.insert.value(_.boolean, 5)" shouldNot compile
  }

  it should "not allow chaining 2 limit clauses on the same query" in {
    "Primitives.select.all().limit(5).limit(5)" shouldNot compile
  }

  it should "not allow chaining multiple order by clauses on the same query" in {
    val user = gen[UUID]
    """tsTable.select.where(_.id eqs user).orderBy(_.timestamp.desc).orderBy(_.timestamp.desc)""" shouldNot compile
  }

  it should "not allow chaining where clauses on the same query, it should only allow where .. and constructs" in {
    val user = gen[UUID]
    """tsTable.select.where(_.id eqs user).where(_.id eqs user)""" shouldNot compile
  }

  it should "not allow specifying multiple consistency bounds on the same query" in {
    val user = gen[UUID]
    """tsTable.select.where(_.id eqs user).consistencyLevel_=(ConsistencyLevel.ONE).consistencyLevel_=(ConsistencyLevel.ONE)""" shouldNot compile
  }
}
