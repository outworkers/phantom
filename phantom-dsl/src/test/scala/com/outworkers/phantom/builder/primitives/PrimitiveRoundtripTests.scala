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

import org.scalacheck.{Arbitrary, Gen}
import com.datastax.driver.core.{LocalDate, ProtocolVersion}

class PrimitiveRoundtripTests extends FlatSpec
  with Matchers
  with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 100)
  }

  private[this] val protocol = ProtocolVersion.V5

  def roundtrip[T : Primitive](gen: Gen[T]): Assertion = {
    val ev = Primitive[T]
    forAll(gen) { sample =>
      ev.deserialize(ev.serialize(sample, protocol), protocol) shouldEqual sample
    }
  }

  def sroundtrip[T : Primitive : Sample]: Assertion = {
    roundtrip[T](Sample.arbitrary[T].arbitrary)
  }

  def roundtrip[T : Primitive : Arbitrary]: Assertion = {
    val ev = Primitive[T]
    forAll { sample: T =>
      ev.deserialize(ev.serialize(sample, protocol), protocol) shouldEqual sample
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

  it should "serialize and deserialize a List[Int] primitive" in {
    roundtrip[List[Int]]
  }

  it should "serialize and deserialize a List[String] primitive" in {
    roundtrip[List[String]]
  }

  it should "serialize and deserialize a List[Double] primitive" in {
    roundtrip[List[Double]]
  }

  it should "serialize and deserialize a List[Long] primitive" in {
    roundtrip[List[Long]]
  }

  it should "serialize and deserialize a List[Float] primitive" in {
    roundtrip[List[Float]]
  }

  it should "serialize and deserialize a List[BigDecimal] primitive" in {
    roundtrip[List[BigDecimal]]
  }

  it should "serialize and deserialize a List[BigInt] primitive" in {
    roundtrip[List[BigInt]]
  }

  it should "serialize and deserialize a List[InetAddress] primitive" in {
    roundtrip[List[InetAddress]]
  }


  it should "serialize and deserialize a Set[Int] primitive" in {
    roundtrip[Set[Int]]
  }

  it should "serialize and deserialize a Set[String] primitive" in {
    roundtrip[Set[String]]
  }

  it should "serialize and deserialize a Set[Double] primitive" in {
    roundtrip[Set[Double]]
  }

  it should "serialize and deserialize a Set[Long] primitive" in {
    roundtrip[Set[Long]]
  }

  it should "serialize and deserialize a Set[Float] primitive" in {
    roundtrip[Set[Float]]
  }

  it should "serialize and deserialize a Set[BigDecimal] primitive" in {
    roundtrip[Set[BigDecimal]]
  }

  it should "serialize and deserialize a Set[BigInt] primitive" in {
    roundtrip[Set[BigInt]]
  }

  it should "serialize and deserialize a Set[InetAddress] primitive" in {
    roundtrip[Set[InetAddress]]
  }

  it should "serialize and deserialize a Map[String, Int] primitive" in {
    roundtrip[Map[String, Int]]
  }

  it should "serialize and deserialize a Map[String, String] primitive" in {
    roundtrip[Map[String, String]]
  }

  it should "serialize and deserialize a Map[String, Double] primitive" in {
    roundtrip[Map[String, Double]]
  }

  it should "serialize and deserialize a Map[String, Long] primitive" in {
    roundtrip[Map[String, Long]]
  }

  it should "serialize and deserialize a Map[String, Float] primitive" in {
    roundtrip[Map[String, Float]]
  }

  it should "serialize and deserialize a Map[String, BigDecimal] primitive" in {
    roundtrip[Map[String, BigDecimal]]
  }

  it should "serialize and deserialize a Map[String, BigInt] primitive" in {
    roundtrip[Map[String, BigInt]]
  }

  it should "serialize and deserialize a Map[String, InetAddress] primitive" in {
    roundtrip[Map[String, InetAddress]]
  }

  it should "serialize and deserialize a tuple (String, Int) primitive" in {
    roundtrip[(String, Int)]
  }

  it should "serialize and deserialize a tuple (String, Int, Date) primitive" in {
    roundtrip[(String, Int, Date)]
  }

  it should "serialize and deserialize a derived Primitive" in {
    sroundtrip[Record]
  }

}
