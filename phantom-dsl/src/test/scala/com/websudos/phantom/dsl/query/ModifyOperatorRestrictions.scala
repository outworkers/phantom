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

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.datastax.driver.core.utils.UUIDs
import com.websudos.phantom.Implicits._
import com.websudos.phantom.tables.{ CounterTableTest, TimeSeriesTable, TwoKeys }
import com.websudos.util.testing.Sampler

class ModifyOperatorRestrictions extends FlatSpec with Matchers with ParallelTestExecution {
  
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
    """TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo "test")""" should compile
  }

  it should "not allow using the setTo operator on a Clustering column" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.timestamp setTo new DateTime)" shouldNot compile
  }

  it should "not allow chaining 2 modify operators on a single update query" in {
   val update = Sampler.getARandomString
   "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo Sampler.getARandomString).modify(_.name setTo Sampler.getARandomString)" shouldNot compile
  }

  it should """allow chaining one "modify" operator followed by one "and" operator on a single update query""" in {
    val update = Sampler.getARandomString
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo Sampler.getARandomString).and(_.name setTo Sampler.getARandomString)" should compile
  }

  it should """allow chaining one "modify" operator followed by multiple "and" operators on a single update query""" in {
    val update = Sampler.getARandomString
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).modify(_.name setTo Sampler.getARandomString).and(_.name setTo Sampler.getARandomString).and(_.name setTo Sampler.getARandomString).and(_.name setTo Sampler.getARandomString)" should compile
  }
}
