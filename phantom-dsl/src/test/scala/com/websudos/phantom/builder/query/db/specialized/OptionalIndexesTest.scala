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
package com.websudos.phantom.builder.query.db.specialized

import com.datastax.driver.core.exceptions.InvalidQueryException
import com.websudos.phantom.PhantomSuite
import com.websudos.phantom.tables.OptionalSecondaryRecord
import com.websudos.phantom.dsl._
import com.outworkers.util.testing._

class OptionalIndexesTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.optionalIndexesTable.insertSchema()
  }

  it should "store a record and then retrieve it using an optional index" in {
    val sample = OptionalSecondaryRecord(
      gen[UUID],
      genOpt[Int]
    )

    val chain = for {
      store <- database.optionalIndexesTable.store(sample)
      get <- database.optionalIndexesTable.findById(sample.id)
      get2 <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
    } yield (get, get2)

    whenReady(chain) {
      case (byId, byIndex) => {
        byId shouldBe defined
        byId.value shouldEqual sample

        byIndex shouldBe defined
        byIndex.value shouldEqual sample
      }
    }
  }

  it should "not be able to delete records by their secondary index" in {
    val sample = OptionalSecondaryRecord(
      gen[UUID],
      genOpt[Int]
    )

    val chain = for {
      store <- database.optionalIndexesTable.store(sample)
      get <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
      delete <- database.optionalIndexesTable.delete.where(_.secondary eqs sample.secondary.value).future()
      get2 <- database.optionalIndexesTable.findByOptionalSecondary(sample.secondary.value)
    } yield (get, get2)

    chain.failing[InvalidQueryException]
  }

}
