/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.query.compilation

import com.websudos.phantom.connectors.KeySpace
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.outworkers.util.testing._
import org.scalatest.{FlatSpec, Matchers, ParallelTestExecution}

class ModifyOperatorRestrictions extends FlatSpec with Matchers with ParallelTestExecution {

  implicit val keySpace: KeySpace = new KeySpace("phantom")

  val TimeSeriesTable = TestDatabase.timeSeriesTable
  val CounterTableTest = TestDatabase.counterTableTest
  val TwoKeys = TestDatabase.multipleKeysTable$
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
