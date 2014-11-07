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

import org.scalatest.{FlatSpec, Matchers}
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.Primitives
import com.websudos.util.testing._

class WhereClauseBuilderTest extends FlatSpec with Matchers {


  val s = gen[String]
  val p = Primitives
  val b = BatchStatement

  it should "allow using a Select.Where clause" in {
    "Primitives.select.where(_.pkey eqs gen[String])" should compile
  }

  it should "allow using a Select.Where clause with AND chain" in {
    "Primitives.select.where(_.pkey eqs gen[String]).and(_.pkey eqs gen[String])" should compile
  }

  it should "not allow chaining two  Select.Where clauses" in {
    "Primitives.select.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "not allow re-using a Where clause after an WHERE/AND chain" in {
    "Primitives.select.where(_.pkey eqs gen[String]).and(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "Should not allow chaining two Update.Where clauses" in {
    "Primitives.update.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }

  it should "not allow chaining two Delete.Where clauses" in {
    "Primitives.update.where(_.pkey eqs gen[String]).where(_.pkey eqs gen[String])" shouldNot compile
  }
}
