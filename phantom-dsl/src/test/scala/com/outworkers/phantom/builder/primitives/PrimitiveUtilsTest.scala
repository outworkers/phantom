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

import org.scalatest._
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{FlatSpec, Matchers}
import com.outworkers.util.samplers._
import org.joda.time.{DateTime, DateTimeZone}
import java.net.InetAddress
import java.util.{Date, UUID}
import java.nio.ByteBuffer
import org.scalacheck.{Arbitrary, Gen}
import com.datastax.driver.core.{ CodecUtils, LocalDate, ProtocolVersion }
import scala.collection.generic.CanBuildFrom

class PrimitiveUtilsTest extends FlatSpec
  with Matchers
  with GeneratorDrivenPropertyChecks
  with PrimitiveSamplers {

  implicit override val generatorDrivenConfig = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  private[this] val protocol = ProtocolVersion.V5

  /**
    * We use this test to check that we rare mainting
    */
  def roundtrip[M[X] <: Traversable[X], T](gen: Gen[T])(
     implicit ev: Primitive[T],
     cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Assertion = {
    forAll(Gen.listOf(gen)) { coll =>
      val bbs = coll.foldRight(Seq.empty[ByteBuffer]) { (elt, acc) =>
        acc :+ ev.serialize(elt, protocol)
      }

      val driverBuffer = CodecUtils.pack(bbs.toArray, coll.size, protocol)
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

  it should "serialize a List[Date] just like the underlying Java Driver" in {
    roundtrip[List, Date]
  }

  it should "serialize a List[UUID] just like the underlying Java Driver" in {
    roundtrip[List, UUID](Gen.uuid)
  }

  it should "serialize a List[InetAddress] just like the underlying Java Driver" in {
    roundtrip[List, InetAddress]
  }

  it should "serialize a List[InetAddress] just like the underlying Java Driver" in {
    roundtrip[List, DateTime]
  }

  it should "serialize a List[InetAddress] just like the underlying Java Driver" in {
    roundtrip[List, LocalDate]
  }
}
