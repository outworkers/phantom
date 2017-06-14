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
package com.outworkers.phantom.builder.query.db.crud

import com.outworkers.phantom.PhantomSuite
import com.outworkers.util.samplers._
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.{Recipe, SampleEvent, ScalaPrimitiveMapRecord}
import org.joda.time.DateTime

class MapOperationsTest extends PhantomSuite {

  override def beforeAll(): Unit = {
    super.beforeAll()
    database.recipes.createSchema()
    database.events.createSchema()
    database.scalaPrimitivesTable.createSchema()
  }

  it should "support a single item map put operation" in {
    val recipe = gen[Recipe]
    val item = gen[(String, String)]

    val operation = for {
      insertDone <- database.recipes.store(recipe).future()
      update <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props put item).future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.props + item
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

    whenReady(operation) { items =>
      items.value shouldEqual recipe.props ++ mapItems
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

    whenReady(chain) { res =>
      res.value shouldEqual sample
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

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate.value shouldEqual sample
      afterUpdate.value shouldEqual sample.copy(map = sample.map + (updateKey -> BigDecimal(updatedValue)))
    }
  }
}
