/*
 * Copyright 2013-2017 Outworkers, Limited.
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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.util.testing._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{Recipe, SampleEvent, ScalaPrimitiveMapRecord}
import org.joda.time.DateTime

class MapOperationsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.insertSchema()
    database.events.insertSchema()
    database.scalaPrimitivesTable.insertSchema()
  }

  it should "support a single item map put operation" in {
    val recipe = gen[Recipe]
    val item = gen[String, String]

    val operation = for {
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props put item).future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield {
      select
    }

    operation.successful {
      items => {
        items.value shouldEqual recipe.props + item
      }
    }
  }

  it should "support a multiple item map put operation" in {
    val recipe = gen[Recipe]
    val mapSize = 5
    val mapItems = genMap[String, String](mapSize)

    val operation = for {
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props putAll mapItems).future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) {
      items => items.value shouldEqual recipe.props ++ mapItems
    }
  }

  it should "support maps of nested primitives" in {
    val event = gen[SampleEvent]

    val chain = for {
      store <- database.events.store(event).future()
      get <- database.events.findById(event.id)
    } yield get

    whenReady(chain) {
      res => res.value shouldEqual event
    }
  }

  it should "allow storing maps that use Scala primitives who do not have a TypeCodec" in {
    val sample = ScalaPrimitiveMapRecord(
      gen[UUID],
      Map(
        gen[DateTime] -> BigDecimal(5),
        gen[DateTime].plusMinutes(2) -> BigDecimal(10),
        gen[DateTime].plusMinutes(2) -> BigDecimal(15)
      )
    )

    val chain = for {
      store <- database.scalaPrimitivesTable.store(sample).future()
      get <- database.scalaPrimitivesTable.findById(sample.id)
    } yield get

    whenReady(chain) {
      res => res.value shouldEqual sample
    }
  }

  it should "allow updating maps that use Scala primitive types" in {

    val updateKey = gen[DateTime]
    val updatedValue = 20

    val sample = ScalaPrimitiveMapRecord(
      gen[UUID],
      Map(
        updateKey -> BigDecimal(5),
        gen[DateTime].plusMinutes(2) -> BigDecimal(10),
        gen[DateTime].plusMinutes(2) -> BigDecimal(15)
      )
    )

    val chain = for {
      store <- database.scalaPrimitivesTable.store(sample).future()
      get <- database.scalaPrimitivesTable.findById(sample.id)
      update <- database.scalaPrimitivesTable.update
        .where(_.id eqs sample.id)
        .modify(_.map(updateKey) setTo BigDecimal(updatedValue))
        .future()
      get2 <- database.scalaPrimitivesTable.findById(sample.id)
    } yield (get, get2)

    whenReady(chain) {
      case (beforeUpdate, afterUpdate) => {
        beforeUpdate.value shouldEqual sample
        afterUpdate.value shouldEqual sample.copy(map = sample.map + (updateKey -> BigDecimal(updatedValue)))
      }
    }
  }
}
