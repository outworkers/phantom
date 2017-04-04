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
import org.joda.time.{ DateTime, DateTimeZone }
import java.net.InetAddress
import java.util.{ Date, UUID }
import org.scalacheck.{ Arbitrary, Gen }
import com.datastax.driver.core.LocalDate

class PrimitiveRoundtripTests extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {
  implicit override val generatorDrivenConfig = {
    PropertyCheckConfiguration(minSuccessful = 100)
  }

  private[this] val genLower: Int = -100000
  private[this] val genHigher: Int = -genLower

  private[this] val inetLowerLimit = 0
  private[this] val inetUpperLimit = 255

  implicit val dateTimeGen: Gen[DateTime] = for {
    offset <- Gen.choose(genLower, genHigher)
    time = new DateTime(DateTimeZone.UTC)
  } yield time.plusMillis(offset)

  implicit val javaDateGen: Gen[Date] = dateTimeGen.map(_.toDate)

  implicit val localDateGen: Gen[LocalDate] = dateTimeGen.map(dt =>
    LocalDate.fromMillisSinceEpoch(dt.getMillis)
  )

  implicit val inetAddressGen: Gen[InetAddress] = {
    for {
      ip1 <- Gen.choose(inetLowerLimit, inetUpperLimit)
      ip2 <- Gen.choose(inetLowerLimit, inetUpperLimit)
      ip3 <- Gen.choose(inetLowerLimit, inetUpperLimit)
      ip4 <- Gen.choose(inetLowerLimit, inetUpperLimit)
    } yield InetAddress.getByName(s"$ip1.$ip2.$ip3.$ip4")
  }

  implicit val inetAddressArb: Arbitrary[InetAddress] = Arbitrary(inetAddressGen)


  def roundtrip[T : Primitive](gen: Gen[T]) = {
    val ev = Primitive[T]
    forAll(gen) { sample =>
      ev.deserialize(ev.serialize(sample)) shouldEqual sample
    }
  }

  def roundtrip[T : Primitive : Arbitrary] = {
    val ev = Primitive[T]
    forAll { sample: T =>
      ev.deserialize(ev.serialize(sample)) shouldEqual sample
    }
  }

  it should "serialize and deserialize a String primitive" in {
    roundtrip[String]
  }

  it should "serialize and deserialize a Int primitive" in {
    roundtrip[Int]
  }

  it should "serialize and deserialize a Double primitive" in {
    roundtrip[Double]
  }

  it should "serialize and deserialize a Float primitive" in {
    roundtrip[Float]
  }

  it should "serialize and deserialize a Long primitive" in {
    roundtrip[Long]
  }

  it should "serialize and deserialize a DateTime primitive" in {
    roundtrip(dateTimeGen)
  }

  it should "serialize and deserialize a java.util.Date primitive" in {
    roundtrip(javaDateGen)
  }

  it should "serialize and deserialize a boolean primitive" in {
    roundtrip[Boolean]
  }

  it should "serialize and deserialize a UUID primitive" in {
    roundtrip(Gen.uuid)
  }

  it should "serialize and deserialize a java.net.InetAddress primitive" in {
    roundtrip(inetAddressGen)
  }

  it should "serialize and deserialize a BigDecimal primitive" in {
    roundtrip[BigDecimal]
  }

  it should "serialize and deserialize a BigInt primitive" in {
    roundtrip[BigInt]
  }

  it should "serialize and deserialie a List[Int] primitive" in {
    roundtrip[List[Int]]
  }

  it should "serialize and deserialie a List[String] primitive" in {
    roundtrip[List[String]]
  }

  it should "serialize and deserialie a List[Double] primitive" in {
    roundtrip[List[Double]]
  }

  it should "serialize and deserialie a List[Long] primitive" in {
    roundtrip[List[Long]]
  }

  it should "serialize and deserialie a List[Float] primitive" in {
    roundtrip[List[Float]]
  }

  it should "serialize and deserialie a List[BigDecimal] primitive" in {
    roundtrip[List[BigDecimal]]
  }

  it should "serialize and deserialie a List[BigInt] primitive" in {
    roundtrip[List[BigInt]]
  }

  it should "serialize and deserialie a List[InetAddress] primitive" in {
    roundtrip[List[InetAddress]]
  }
}
