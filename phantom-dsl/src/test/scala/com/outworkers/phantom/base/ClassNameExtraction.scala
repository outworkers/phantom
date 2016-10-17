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
package com.outworkers.phantom.base

import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.phantom.dsl._

case class CustomRecord(name: String, mp: Map[String, String])

trait TestTableNames extends CassandraTable[TestTableNames, CustomRecord] {
  object rec extends StringColumn(this) with PartitionKey[String]
  object sampleLongTextColumnDefinition extends MapColumn[String, String](this)

  override def fromRow(r: Row): CustomRecord = {
    CustomRecord(
      rec(r),
      sampleLongTextColumnDefinition(r)
    )
  }
}

object TestTableNames extends TestTableNames

object Test extends PrimitiveColumn[TestTableNames, CustomRecord, String](TestTableNames)

trait TestNames extends TestTableNames

class Parent extends TestNames
class Parent2 extends Parent

class ClassNameExtraction extends FlatSpec with Matchers {


  it should "correctly name objects inside record classes " in {
    TestTableNames.rec.name shouldEqual "rec"
  }

  it should "correctly extract long object name definitions in nested record classes" in {
    TestTableNames.sampleLongTextColumnDefinition.name shouldEqual "sampleLongTextColumnDefinition"
  }

  it should "correctly name Cassandra Tables" in {
    TestTableNames.tableName shouldEqual "testTableNames"
  }

  it should "correctly extract the object name " in {
    Test.name shouldEqual "Test"
  }
}
