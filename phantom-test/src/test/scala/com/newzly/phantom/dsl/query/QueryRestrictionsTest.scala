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

import org.scalatest.{ParallelTestExecution, FlatSpec, Matchers}
import com.newzly.phantom.tables._

class QueryRestrictionsTest extends FlatSpec with Matchers with ParallelTestExecution {

  it should "not allow using a wrong type for a value method" in {
    "Primitives.insert.value(_.boolean, 5).future()" shouldNot compile
  }

  it should "not allow using the eqs operator on non index columns" in {
    "Primitives.select.where(_.long eqs 5L).one()" shouldNot compile
  }

  it should "not allow using the lt operator on non index columns" in {
    "Primitives.select.where(_.long lt 5L).one()" shouldNot compile
  }

  it should "not allow using the lte operator on non index columns" in {
    "Primitives.select.where(_.long lte 5L).one()" shouldNot compile
  }

  it should "not allow using the gt operator on non index columns" in {
    "Primitives.select.where(_.long gt 5L).one()" shouldNot compile
  }

  it should "not allow using the gte operator on non index columns" in {
    "Primitives.select.where(_.long gte 5L).one()" shouldNot compile
  }

  it should "not allow using the in operator on non index columns" in {
    "Primitives.select.where(_.long in List(5L, 6L)).one()" shouldNot compile
  }

  it should "not allow using the setTo operator on a Counter column" in {
    "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.count_entries setTo 5L)" shouldNot compile
  }

  it should "not allow using the setTo operator on a PartitionKey" in {
    "CounterTableTest.update.where(_.id eqs UUIDs.timeBased()).modify(_.id setTo UUIDs.timeBased())" shouldNot compile
  }

  it should "not allow using the setTo operator on a PrimaryKey" in {
    "TwoKeys.update.where(_.pkey eqs UUIDs.timeBased().toString).modify(_.pkey setTo UUIDs.timeBased().toString)" shouldNot compile
  }

  it should "allow using setTo operators for non index columns" in {
    """TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo "test")""" shouldNot compile
  }

  it should "not allow using the setTo operator on a Clustering column" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.timestamp setTo new DateTime)" shouldNot compile
  }

  it should "not allow using Select queries in a batch" in {
    "BatchStatement().add(Primitives.select)" shouldNot compile
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
