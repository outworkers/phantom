/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

  it should "support a single item map set operation" in {
    val recipe = gen[Recipe]
    val (key , value) = gen[(String, String)]

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props set (key, value)).future()

      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.get(key) shouldBe defined
      items.value.get(key).value shouldEqual value
    }
  }


  it should "support a single item map set operation using prepared statements" in {
    val recipe = gen[Recipe]
    val (key , value) = gen[(String, String)]

    val query = database.recipes.update.where(_.url eqs ?).modify(_.props set ?).prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- query.flatMap(_.bind(key, value, recipe.url).future())
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value.get(key) shouldBe defined
      items.value.get(key).value shouldEqual value
    }
  }



  it should "support a single item map put operation" in {
    val recipe = gen[Recipe]
    val item = gen[(String, String)]

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props put item).future()
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
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update.where(_.url eqs recipe.url).modify(_.props putAll mapItems).future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.props ++ mapItems
    }
  }

  it should "support a multiple item map put operation with prepared statements" in {
    val recipe = gen[Recipe]
    val mapSize = 5
    val mapItems = genMap[String, String](mapSize)

    val updateQuery = database.recipes.update.where(_.url eqs ?).modify(_.props putAll ?).prepareAsync()

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- updateQuery.flatMap(_.bind(mapItems, recipe.url).future())
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual recipe.props ++ mapItems
    }
  }


  it should "support removing a single key from a map" in {
    val recipe = gen[Recipe]
    val removals = recipe.props.keys.headOption.value
    val postRemove = recipe.props - removals

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update
        .where(_.url eqs recipe.url)
        .modify(_.props - removals)
        .future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual postRemove
    }
  }

  it should "support a multiple item map key remove operation" in {
    val recipe = gen[Recipe]
    val removals = recipe.props.take(2).keys.toSeq
    val postRemove = removals.foldLeft(recipe.props) { case (map, el) =>
      map - el
    }

    val operation = for {
      _ <- database.recipes.store(recipe).future()
      _ <- database.recipes.update
        .where(_.url eqs recipe.url)
        .modify(_.props - removals)
        .future()
      select <- database.recipes.select(_.props).where(_.url eqs recipe.url).one
    } yield select

    whenReady(operation) { items =>
      items.value shouldEqual postRemove
    }
  }

  it should "support maps of nested primitives" in {
    val event = gen[SampleEvent]

    val chain = for {
      _ <- database.events.store(event).future()
      res <- database.events.findById(event.id)
    } yield res

    whenReady(chain) { res => res.value shouldEqual event }
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
      _ <- database.scalaPrimitivesTable.store(sample).future()
      res <- database.scalaPrimitivesTable.findById(sample.id)
    } yield res

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
      _ <- database.scalaPrimitivesTable.store(sample).future()
      res <- database.scalaPrimitivesTable.findById(sample.id)
      _ <- database.scalaPrimitivesTable.update
        .where(_.id eqs sample.id)
        .modify(_.map(updateKey) setTo BigDecimal(updatedValue))
        .future()
      res2 <- database.scalaPrimitivesTable.findById(sample.id)
    } yield (res, res2)

    whenReady(chain) { case (beforeUpdate, afterUpdate) =>
      beforeUpdate.value shouldEqual sample
      afterUpdate.value shouldEqual sample.copy(map = sample.map + (updateKey -> BigDecimal(updatedValue)))
    }
  }
}
