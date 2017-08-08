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

import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.{CodecUtils, LocalDate, ProtocolVersion}
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers, _}

import scala.collection.generic.CanBuildFrom

/**
  * Test suite to check for serialization parity in between phantom and the java driver.
  * We do this to make sure we are fully compatible with any underlying changes to the driver
  * and to the Cassandra binary protocol.
  */
class PrimitiveUtilsTest extends FlatSpec
  with Matchers
  with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  private[this] val protocol = ProtocolVersion.V5

  def serialize[M[X] <: Traversable[X],T](
    input: M[T],
    version: ProtocolVersion = ProtocolVersion.V5
  )(
    implicit ev: Primitive[T],
    cbf: CanBuildFrom[M[T], ByteBuffer, M[ByteBuffer]]
  ): M[ByteBuffer] = {
    input.foldRight(cbf()) { (elt, acc) =>
      acc += ev.serialize(elt, version)
    } result()
  }

  /**
    * We use this test to check that we are maintaining
    * serialization compatibility with the underlying Java driver.
    */
  def roundtrip[M[X] <: Traversable[X], T](gen: Gen[T])(
     implicit ev: Primitive[T],
     cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Assertion = {
    forAll(Gen.listOf(gen)) { coll =>
      val bbs = serialize(coll, protocol)
      val driverBuffer = CodecUtils.pack(bbs.toArray, bbs.size, protocol)
      val phantomBuffer = Utils.pack(bbs, coll.size, protocol)
      driverBuffer shouldEqual phantomBuffer
    }
  }

  def roundtrip[M[X] <: Traversable[X], T]()(
     implicit ev: Primitive[T],
     arb: Arbitrary[T],
     cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Assertion = roundtrip(arb.arbitrary)(ev, cbf)

  it should "serialize a List[String] just like the underlying Java Driver" in {
    roundtrip[List, String]
  }

  it should "serialize a List[Int] just like the underlying Java Driver" in {
    roundtrip[List, Int]
  }

  it should "serialize a List[Double] just like the underlying Java Driver" in {
    roundtrip[List, Double]
  }

  it should "serialize a List[Float] just like the underlying Java Driver" in {
    roundtrip[List, Float]
  }

  it should "serialize a List[Long] just like the underlying Java Driver" in {
    roundtrip[List, Long]
  }

  it should "serialize a List[UUID] just like the underlying Java Driver" in {
    roundtrip[List, UUID](Gen.uuid)
  }

  it should "serialize a List[InetAddress] just like the underlying Java Driver" in {
    roundtrip[List, InetAddress]
  }

  it should "serialize a List[ByteBuffer] just like the underlying Java Driver" in {
    roundtrip[List, ByteBuffer](bytebufferGen[String](protocol))
  }

  it should "serialize a List[Date] just like the underlying Java Driver" in {
    roundtrip[List, Date]
  }

  it should "serialize a List[DateTime] just like the underlying Java Driver" in {
    roundtrip[List, DateTime](dateTimeGen)
  }

  it should "serialize a List[LocalDate] just like the underlying Java Driver" in {
    roundtrip[List, LocalDate](localDateGen)
  }
}
