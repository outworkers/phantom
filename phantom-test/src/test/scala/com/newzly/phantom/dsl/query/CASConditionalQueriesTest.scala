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
import com.datastax.driver.core.utils.UUIDs
import com.newzly.phantom.Implicits._
import com.newzly.phantom.tables.{ Primitives, SecondaryIndexTable, TimeSeriesTable }
import com.newzly.util.testing.Sampler

class CASConditionalQueriesTest extends FlatSpec with Matchers {
  it should "allow using a non-index column in a conditional update clause" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L)" should compile
  }

  it should " not allow using a PartitionKey in a conditional clause" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should " not allow using a PrimaryKey in a conditional clause " in {
    "TwoKeys.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.intColumn1 eqs 5)" shouldNot compile
  }

  it should " not allow using an Index in a conditional clause " in {
    "SecondaryIndexTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.secondary eqs UUIDs.timeBased())" shouldNot compile
  }

  it should " not allow using an Index in the second part of a conditional clause " in {
    "SecondaryIndexTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.name eqs Sampler.getARandomString).and(_.secondary eqs UUIDs.timeBased())" shouldNot compile
  }

  it should " allow using a non Clustering column from a TimeSeries table in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.name eqs Sampler.getARandomString)" should compile
  }

  it should " not allow using a ClusteringColumn in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.timestamp eqs new DateTime)" shouldNot compile
  }

  it should " not allow using a ClusteringColumn in the second part of a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs UUIDs.timeBased()).onlyIf(_.name eqs Sampler.getARandomString).and(_.timestamp eqs new DateTime)" shouldNot compile
  }

  it should "allow using multiple non-primary conditions in a CAS clase" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L).and(_.boolean eqs false)" should compile
  }

  it should "not allow using an index column condition in the AND part of a CAS clause" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L).and(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }

  it should "allow using 3 separate CAS conditions in an update query" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L).and(_.boolean eqs false).and(_.int eqs 10)" should compile
  }

  it should "not allow using 3 separate CAS conditions in an update query with the 3rd condition on an indexed column" in {
    "Primitives.update.where(_.pkey eqs Sampler.getARandomString).onlyIf(_.long eqs 5L).and(_.boolean eqs false).and(_.pkey eqs Sampler.getARandomString)" shouldNot compile
  }
}
