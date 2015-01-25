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
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
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
package com.websudos.phantom.dsl

import org.scalatest.{ FlatSpec, Matchers, ParallelTestExecution }
import com.websudos.phantom.tables.{
  Primitives,
  Recipes,
  TestTable,
  TwoKeys
}

class CreateTableQueryString extends FlatSpec with Matchers with ParallelTestExecution {
  it should "create the right keys" in {
    val q = TwoKeys.schema()
    assert(q.contains("PRIMARY KEY (pkey, " +
      "intColumn1, " +
      "intColumn2, " +
      "intColumn3, " +
      "intColumn4, " +
      "intColumn5, " +
      "intColumn6, " +
      "intColumn7" +
      ")"))
  }

  it should "get the right query in primitives table" in {
    val q = Primitives.schema()
    val manual = s"CREATE TABLE ${Primitives.tableName}} " +
        "( pkey int, " +
        "longName bigint, " +
        "boolean boolean, " +
        "bDecimal decimal, " +
        "double double, " +
        "float float, " +
        "inet inet, " +
        "int int, " +
        "date timestamp, " +
        "uuid uuid, " +
        "bi varint, " +
        "PRIMARY KEY (pkey));"
    assert(Primitives.columns.forall(column => { manual.contains(column.name) }))
    assert(Primitives.columns.forall(column => { q.contains(column.name) }))
  }

  it should "get the correct count the primitives table" in {
    Primitives.columns.length shouldEqual 11
  }

  it should "work fine with List, Set, Map" in {
    TestTable.schema() shouldEqual "CREATE TABLE IF NOT EXISTS TestTable ( key text, list list<text>, setText set<text>, mapTextToText map<text, text>, setInt set<int>, mapIntToText map<int, text>, PRIMARY KEY (key));"
  }

  it should "get the right query in mix table" in {
    Recipes.schema() shouldEqual "CREATE TABLE IF NOT EXISTS Recipes ( url text, description text, ingredients list<text>, servings int, last_checked_at timestamp, props map<text, text>, uid uuid, PRIMARY KEY (url));"
  }
}

