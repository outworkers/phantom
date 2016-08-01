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
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
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
package com.websudos.phantom.builder.query.compilation

import com.datastax.driver.core.ConsistencyLevel
import com.websudos.phantom.builder.query.SerializationTest
import com.websudos.phantom.dsl.UUID
import com.websudos.phantom.tables.TestDatabase
import org.scalatest.FlatSpec
import com.outworkers.util.testing._

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
