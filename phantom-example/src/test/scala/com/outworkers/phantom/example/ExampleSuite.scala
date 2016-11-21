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
package com.outworkers.phantom.example

import java.util.UUID

import com.outworkers.phantom.connectors.RootConnector
import com.outworkers.phantom.Manager._
import com.outworkers.phantom.dsl.DatabaseProvider
import com.outworkers.phantom.example.advanced.RecipesDatabase
import com.outworkers.phantom.example.basics.Recipe
import com.outworkers.util.lift.{DateTimeSerializer, UUIDSerializer}
import com.outworkers.util.testing._
import org.joda.time.DateTime
import org.scalatest.concurrent.{PatienceConfiguration, ScalaFutures}
import org.scalatest._

import scala.concurrent.duration._

trait BaseSuite extends Suite
  with Matchers
  with BeforeAndAfterAll
  with RootConnector
  with ScalaFutures
  with OptionValues {

  implicit val defaultTimeout: PatienceConfiguration.Timeout = timeout(10 seconds)

  implicit val formats = net.liftweb.json.DefaultFormats + new UUIDSerializer + new DateTimeSerializer
}

trait RecipesDbProvider extends DatabaseProvider[RecipesDatabase] {
  override def database: RecipesDatabase = RecipesDatabase
}

trait ExampleSuite extends Suite with BaseSuite with RecipesDatabase.connector.Connector with RecipesDbProvider {

  override def beforeAll(): Unit = {
    super.beforeAll()
    RecipesDatabase.create()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    RecipesDatabase.truncate()
  }

  implicit object RecipeSampler extends Sample[Recipe] {
    override def sample: Recipe = Recipe(
      id = gen[UUID],
      name = gen[ShortString].value,
      title = gen[ShortString].value,
      author = gen[ShortString].value,
      description = gen[ShortString].value,
      ingredients = genList[String]().toSet,
      props = genMap[String](),
      timestamp = gen[DateTime]
    )
  }
}


