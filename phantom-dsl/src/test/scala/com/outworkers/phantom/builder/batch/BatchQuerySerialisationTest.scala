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
package com.outworkers.phantom.builder.batch

import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.JodaRow
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec

class BatchQuerySerialisationTest extends FlatSpec with SerializationTest {

  ignore should "serialize a multiple table batch query applied to multiple statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = db.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo row2.timestamp)

    val statement4 = db.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3, statement4).queryString

    batch shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.intColumn}," +
      s" timestamp = ${row2.timestamp.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda" +
      s" WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

  ignore should "serialize a multiple table batch query chained from adding statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = db.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.intColumn)
      .and(_.timestamp setTo row2.timestamp)

    val statement4 = db.primitivesJoda.delete.where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)
    batch.queryString shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.intColumn}," +
      s" timestamp = ${row2.timestamp.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda" +
      s" WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

}
