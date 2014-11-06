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

class IndexOperatorsRestrictionTests extends FlatSpec with Matchers {

  val s = gen[String]
  val p = Primitives
  val b = BatchStatement
  
  it should "allow using the eqs operator on index columns" in {
    "Primitives.select.where(_.pkey eqs gen[String])" should compile
  }

  it should "not allow using the eqs operator on non index columns" in {
    "Primitives.select.where(_.long eqs 5L)" shouldNot compile
  }

  it should "allow using the lt operator on index columns" in {
    "Primitives.select.where(_.pkey lt gen[String])" should compile
  }

  it should "not allow using the lt operator on non index columns" in {
    "Primitives.select.where(_.long lt 5L)" shouldNot compile
  }

  it should "allow using the lte operator on index columns" in {
    "Primitives.select.where(_.pkey lte gen[String])" should compile
  }

  it should "not allow using the lte operator on non index columns" in {
    "Primitives.select.where(_.long lte 5L)" shouldNot compile
  }

  it should "allow using the gt operator on index columns" in {
    "Primitives.select.where(_.pkey gt gen[String])" should compile
  }

  it should "not allow using the gt operator on non index columns" in {
    "Primitives.select.where(_.long gt 5L)" shouldNot compile
  }

  it should "allow using the gte operator on index columns" in {
    "Primitives.select.where(_.pkey gte gen[String])" should compile
  }

  it should "not allow using the gte operator on non index columns" in {
    "Primitives.select.where(_.long gte 5L)" shouldNot compile
  }

  it should "allow using the in operator on index columns" in {
    "Primitives.select.where(_.pkey in List(gen[String], gen[String]))" should compile
  }

  it should "not allow using the in operator on non index columns" in {
    "Primitives.select.where(_.long in List(5L, 6L))" shouldNot compile
  }
}
