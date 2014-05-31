/*
 * Copyright 2013 newzly ltd.
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
package com.newzly.phantom.dsl.query

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitives, Recipes }

class BatchRestrictionTest extends FlatSpec with Matchers {
  
  it should "not allow using Select queries in a batch" in {
    "BatchStatement().add(Primitives.select)" shouldNot compile
  }

  it should "not allow using a primary key in a conditional clause" in {
    """Recipes.update.where(_.url eqs "someUrl").modify(_.name setTo "test").onlyIf(_.id eqs secondary)""" shouldNot compile
  }

  it should "not allow using SelectWhere queries in a batch" in {
    "BatchStatement().add(Primitives.select.where(_.pkey eqs Sampler.getARandomString))" shouldNot compile
  }

  it should "not allow using Truncate queries in a batch" in {
    "BatchStatement().add(Primitives.truncate)" shouldNot compile
  }

  it should "not allow using Create queries in a batch" in {
    "BatchStatement().add(Primitives.create)" shouldNot compile
  }
}
