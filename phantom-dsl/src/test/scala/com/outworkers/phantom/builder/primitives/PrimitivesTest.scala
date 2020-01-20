
/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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

import com.datastax.driver.core.ProtocolVersion
import com.datastax.driver.core.exceptions.DriverInternalError
import com.outworkers.phantom.builder.QueryBuilder
import com.outworkers.phantom.builder.query.engine.CQLQuery
import org.joda.time.{DateTime, DateTimeZone}
import org.scalatest.{Assertion, FlatSpec, Matchers}
import com.outworkers.util.samplers._
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.collection.compat.Factory


/**
  * Test suite to check for some special edge cases in primitive generation and serialization.
  *
  */
class PrimitivesTest extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  def serialization[M[X] <: Traversable[X], T](
    fn: M[T] => String
  )(
    implicit arb: Arbitrary[T],
    ev: Primitive[T],
    ev2: Primitive[M[T]],
    cbf: Factory[T, M[T]]
  ): Assertion = {
    val colGen = Gen.buildableOf[M[T], T](arb)
    forAll(colGen) { (col: M[T]) =>
      ev2.asCql(col) shouldEqual fn(col)
    }
  }

  it should "produce an empty bytebuffer for an empty collection" in {
    val empty = Primitives.emptyCollection
    empty.array().size shouldEqual 0
  }

  it should "throw an supported version error when neccessary" in {
    intercept[DriverInternalError] {
      throw Utils.unsupported(ProtocolVersion.V5)
    }
  }

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

  it should "generate a primitive for a collection of tuples" in {
    """
      val primitive = Primitive[List[(String, Int)]]
      val strPrimitive = Primitive[List[String]]

      val samples = genList[Int]() map (i => gen[String] -> i)
      val strSamples = genList[String]()
    """ should compile
  }

  it should "deserialize an empty string to a Some(empty) with optional primitives" in {
    val primitve = Primitive[Option[String]]
    primitve.serialize(None, ProtocolVersion.V5)
    primitve.deserialize(Primitive.nullValue, ProtocolVersion.V5) shouldEqual None
  }

  it should "autogenerate set primitives for Map types" in {
    """val test = Primitive[Map[String, String]]""" should compile
  }

  it should "freeze List collection primitives" in {
    Primitive[List[String]].shouldFreeze shouldEqual true
  }

  it should "freeze Set collection primitives" in {
    Primitive[Set[String]].shouldFreeze shouldEqual true
  }

  it should "freeze Map collection primitives" in {
    Primitive[Map[String, String]].shouldFreeze shouldEqual true
  }

  it should "freeze Tuple collection primitives" in {
    Primitive[(String, String, Int)].shouldFreeze shouldEqual true
  }

  it should "correctly serialize a primitive type" in {
    val sample = gen[(String, String, Int)]
    val qb = Primitive[(String, String, Int)].asCql(sample)
    val (s1, s2, i1) = sample

    qb shouldEqual s"(${Primitive[String].asCql(s1)}, ${Primitive[String].asCql(s2)}, ${Primitive[Int].asCql(i1)})"
  }

  it should "serialize a Set[Int] primitive accordingly" in {
    serialization[Set, Int](set => QueryBuilder.Collections.serialize(set.map(Primitive[Int].asCql)).queryString)
  }

  it should "serialize a Set[String] primitive accordingly" in {
    serialization[Set, String](set => QueryBuilder.Collections.serialize(set.map(Primitive[String].asCql)).queryString)
  }

  it should "serialize a Set[Float] primitive accordingly" in {
    serialization[Set, Float](set => QueryBuilder.Collections.serialize(set.map(Primitive[Float].asCql)).queryString)
  }

  it should "automatically generate a primitive for an enumeration" in {
    object EnumTest extends Enumeration {
      val one = Value("one")
    }

    val test = Primitive[EnumTest.Value]
    test.asCql(EnumTest.one) shouldEqual CQLQuery.escape("one")
  }

  it should "read a bytebuffer with a single size element as an empty collection" in {
    val ev = Primitive[List[String]]
    val bb = ev.serialize(List.empty[String], ProtocolVersion.V4)
    val decoded = ev.deserialize(bb, ProtocolVersion.V4)
    decoded shouldEqual List.empty[String]
  }

  it should "derive a primitive for a custom wrapper type" in {
    val str = gen[String]

    Primitive[DerivedField].asCql(DerivedField(str)) shouldEqual CQLQuery.escape(str)
  }

  it should "automatically generate a primitive for an optional type" in {
    val r = DerivedField
    """val ev = Primitive[Option[DerivedField]]""" should compile
  }
}
