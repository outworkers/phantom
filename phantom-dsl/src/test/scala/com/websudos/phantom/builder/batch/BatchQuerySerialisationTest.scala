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
package com.websudos.phantom.builder.batch

import com.websudos.phantom.builder.query.SerializationTest
import com.websudos.phantom.dsl._
import com.websudos.phantom.tables.{JodaRow, TestDatabase}
import com.websudos.util.testing._
import org.scalatest.FlatSpec

class BatchQuerySerialisationTest extends FlatSpec with SerializationTest {

  ignore should "serialize a multiple table batch query applied to multiple statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = TestDatabase.primitivesJoda.delete
      .where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3, statement4).queryString

    batch shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.int}," +
      s" timestamp = ${row2.bi.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda" +
      s" WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

  ignore should "serialize a multiple table batch query chained from adding statements" in {

    val row = gen[JodaRow]
    val row2 = gen[JodaRow].copy(pkey = row.pkey)
    val row3 = gen[JodaRow]

    val statement3 = TestDatabase.primitivesJoda.update
      .where(_.pkey eqs row2.pkey)
      .modify(_.intColumn setTo row2.int)
      .and(_.timestamp setTo row2.bi)

    val statement4 = TestDatabase.primitivesJoda.delete.where(_.pkey eqs row3.pkey)

    val batch = Batch.logged.add(statement3).add(statement4)
    batch.queryString shouldEqual s"BEGIN BATCH UPDATE phantom.PrimitivesJoda SET intColumn = ${row2.int}," +
      s" timestamp = ${row2.bi.getMillis} WHERE pkey = '${row2.pkey}'; DELETE FROM phantom.PrimitivesJoda" +
      s" WHERE pkey = '${row3.pkey}'; APPLY BATCH;"
  }

}
