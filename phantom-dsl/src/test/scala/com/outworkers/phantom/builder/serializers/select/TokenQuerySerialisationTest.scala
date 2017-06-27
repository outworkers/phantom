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
package com.outworkers.phantom.builder.serializers.select

import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.tables.TestDatabase
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.samplers._

class TokenQuerySerialisationTest extends FlatSpec with SerializationTest with Matchers {

  it should "serialize a single column token clause" in {
    val id = gen[String]

    val qb = TestDatabase.enumTable.select.where(table => token(table.id) > token(id)).queryString

    qb shouldEqual s"SELECT * FROM phantom.enumTable WHERE TOKEN (id) > TOKEN ('$id');"
  }

  it should "serialize a single column prepared token function" in {
    val qb = TestDatabase.enumTable.select.where(table => token(table.id) > token(?))

    qb.queryString shouldEqual s"SELECT * FROM phantom.enumTable WHERE TOKEN (id) > TOKEN (?);"
  }
}
