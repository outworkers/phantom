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
package com.outworkers.phantom.builder.serializers

import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.SerializationTest
import com.outworkers.util.samplers._
import org.scalatest.FlatSpec

class SASIQueryBuilderTest extends FlatSpec with SerializationTest {

  it should "prefix a value clause" in {
    val value = gen[ShortString].value
    val qb = QueryBuilder.SASI.prefixValue(value).queryString

    qb shouldEqual s"'$value%'"
  }

  it should "suffix a value clause" in {
    val value = gen[ShortString].value
    val qb = QueryBuilder.SASI.suffixValue(value).queryString

    qb shouldEqual s"'%$value'"
  }


  it should "serialize a contains value clause" in {
    val value = gen[ShortString].value
    val qb = QueryBuilder.SASI.containsValue(value).queryString

    qb shouldEqual s"'%$value%'"
  }

}
