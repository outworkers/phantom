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

import java.nio.ByteBuffer

import com.outworkers.phantom.builder.QueryBuilder
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.testing._

class PrimitivesTest extends FlatSpec with Matchers {

  it should "coerce a DateTime into a valid timezone string" in {
    val date = new DateTime(2014, 6, 2, 10, 5, DateTimeZone.UTC)

    DateSerializer.asCql(date) shouldEqual date.getMillis.toString
  }

  it should "convert ByteBuffers to valid hex bytes" in {
    val buf = ByteBuffer.wrap(Array[Byte](1, 2, 3, 4, 5))
    Primitive[ByteBuffer].asCql(buf) shouldEqual "0x0102030405"

    buf.position(2)   // Non-zero position
    Primitive[ByteBuffer].asCql(buf) shouldEqual "0x030405"

    val slice = buf.slice()   // Slice with non-zero arrayOffset
    Primitive[ByteBuffer].asCql(slice) shouldEqual "0x030405"
  }

  it should "autogenerate list primitives for List types" in {
    val test = Primitive[List[String]]
    val input = genList[String]()
    val expected = QueryBuilder.Collections.serialize(input.map(Primitive[String].asCql)).queryString

    test.asCql(input) shouldEqual expected
  }

  it should "correctly serialize a tuple using a primitive" in {
    val primitive = implicitly[Primitive[(String, Long)]]
    QueryBuilder.Collections.tupled("'test'", "5").queryString shouldEqual "('test', 5)"
    primitive.asCql("test" -> 5) shouldEqual "('test', 5)"
  }

  it should "autogenerate set primitives for Set types" in {
    """val test = Primitive[Set[String]]""" should compile
  }


  it should "autogenerate set primitives for Map types" in {
    """val test = Primitive[Map[String, String]]""" should compile
  }

}
