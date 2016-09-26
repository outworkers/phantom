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

import com.websudos.phantom.builder.query.KeySpaceSuite
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.TestDatabase
import com.outworkers.util.testing._
import org.scalatest.{FlatSpec, Matchers}

class CompareAndSetRestrictionsTest extends FlatSpec with Matchers with KeySpaceSuite {

  val Primitives = TestDatabase.primitives
  val TimeSeriesTable = TestDatabase.timeSeriesTable
  val SecondaryIndexTable = TestDatabase.secondaryIndexTable
  val TwoKeys = TestDatabase.multipleKeysTable$
  val b = Batch

  it should "allow using a non-index column in a conditional update clause" in {
    val s = gen[String]
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L)" should compile
  }

  it should " not allow using a PartitionKey in a conditional clause" in {
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.pkey is gen[String])" shouldNot compile
  }

  it should " not allow using a PrimaryKey in a conditional clause " in {
    "TwoKeys.update.where(_.pkey eqs gen[String]).onlyIf(_.intColumn1 is 5)" shouldNot compile
  }

  it should " not allow using an Index in a conditional clause " in {
    "SecondaryIndexTable.update.where(_.id eqs gen[UUID]).onlyIf(_.secondary is gen[UUID])" shouldNot compile
  }

  it should " not allow using an Index in the second part of a conditional clause " in {
    "SecondaryIndexTable.update.where(_.id eqs gen[UUID]).onlyIf(_.name is gen[String]).and(_.secondary is gen[UUID])" shouldNot compile
  }

  it should " allow using a non Clustering column from a TimeSeries table in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).onlyIf(_.name is gen[String])" should compile
  }

  it should " not allow using a ClusteringColumn in a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).onlyIf(_.timestamp is new DateTime)" shouldNot compile
  }

  it should " not allow using a ClusteringColumn in the second part of a conditional clause" in {
    "TimeSeriesTable.update.where(_.id eqs gen[UUID]).onlyIf(_.name is gen[String]).and(_.timestamp is new DateTime)" shouldNot compile
  }

  it should "allow using multiple non-primary conditions in a CAS clause" in {
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L).and(_.boolean is false)" should compile
  }

  it should "not allow using an index column condition in the AND part of a CAS clause" in {
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L).and(_.pkey is gen[String])" shouldNot compile
  }

  it should "allow using 3 separate CAS conditions in an update query" in {
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L).and(_.boolean is false).and(_.int is 10)" should compile
  }

  it should "not allow using 3 separate CAS conditions in an update query with the 3rd condition on an indexed column" in {
    "Primitives.update.where(_.pkey eqs gen[String]).onlyIf(_.long is 5L).and(_.boolean is false).and(_.pkey is gen[String])" shouldNot compile
  }
}
