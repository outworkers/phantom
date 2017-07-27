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
package com.outworkers.phantom.builder.query.compilation

import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import com.outworkers.util.samplers._
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}

class ModifyOperatorRestrictions extends FlatSpec with Matchers with ParallelTestExecution {

  implicit val keySpace: KeySpace = KeySpace("phantom")

  val TimeSeriesTable = TestDatabase.timeSeriesTable
  val CounterTableTest = TestDatabase.counterTableTest
  val TwoKeys = TestDatabase.multipleKeysTable
  val update = gen[String]

  it should "not allow using the setTo operator on a Counter column" in {
    "CounterTableTest.update.where(_.id eqs gen[UUID]).modify(_.count_entries setTo 5L)" shouldNot compile
  }

  it should "not allow using the setTo operator on a PartitionKey" in {
    "CounterTableTest.update.where(_.id eqs gen[UUID]).modify(_.id setTo gen[UUID])" shouldNot compile
  }

  it should "not allow using the setTo operator on a PrimaryKey" in {
    "TwoKeys.update.where(_.pkey eqs gen[UUID].toString).modify(_.pkey setTo gen[String])" shouldNot compile
  }

  it should "allow using setTo operators for non index columns" in {
    """TimeSeriesTable.update.where(_.id eqs gen[UUID]).modify(_.name setTo "test")""" should compile
  }

  it should "not allow using the setTo operator on a Clustering column" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).modify(_.timestamp setTo new DateTime)" shouldNot compile
  }

  it should "not allow chaining 2 modify operators on a single update query" in {
   "TimeSeriesTable.update.where(_.id eqs gen[UUID]).modify(_.name setTo gen[String]).modify(_.name setTo gen[String])" shouldNot compile
  }

  it should """allow chaining one "modify" operator followed by one "and" operator on a single update query""" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).modify(_.name setTo gen[String]).and(_.name setTo gen[String])" should compile
  }

  it should """allow chaining one "modify" operator followed by multiple "and" operators on a single update query""" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).modify(_.name setTo gen[String]).and(_.name setTo gen[String]).and(_.name setTo gen[String]).and(_.name setTo gen[String])" should compile
  }
}
