/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.dsl.query

import com.websudos.phantom.tables.Primitives
import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.Implicits._
import com.websudos.util.testing._

class AllowedBatchQueriesTest extends FlatSpec with Matchers {

  val s = gen[String]
  val b = BatchStatement
  val p = Primitives
  
  it should "allow using Insert queries in a Batch statement" in {
    "BatchStatement().add(Primitives.insert)" should compile
  }

  it should " allow using an Insert.Value statement in a BatchStatement" in {
    "BatchStatement().add(Primitives.insert.value(_.long, 4L))" should compile
  }

  it should "allow using an Update.Assignments statement in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.modify(_.long setTo 5L))" should compile
  }

  it should "allow using Update.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs gen[String]))" should compile
  }

  it should "allow using Conditional Update.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long eqs 5L))" should compile
  }

  it should " allow using Conditional Assignments queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.update.where(_.pkey eqs gen[String]).modify(_.long setTo 10L).onlyIf(_.long eqs 5L))" should compile
  }

  it should " allow using Delete queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.delete)" should compile
  }

  it should "Delete.Where queries in a BatchStatement" in {
    "BatchStatement().add(Primitives.delete)" should compile
  }
}
