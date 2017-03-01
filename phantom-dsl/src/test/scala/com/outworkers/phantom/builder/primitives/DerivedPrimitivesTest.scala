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
package com.outworkers.phantom.builder.primitives

import com.outworkers.phantom.PhantomSuite
import com.outworkers.phantom.builder.query.engine.CQLQuery
import com.outworkers.util.samplers._

case class Record(value: String)

object Record {
  implicit val recordPrimitive: Primitive[Record] = Primitive.derive[Record, String](_.value)(Record.apply)
}

class DerivedPrimitivesTest extends PhantomSuite {

  it should "derive a primitive for a custom wrapper type" in {
    val str = gen[String]

    Primitive[Record].asCql(Record(str)) shouldEqual CQLQuery.escape(str)
  }
}
