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
import java.util.Date
import org.scalacheck.{ Arbitrary, Gen }

class PrimitiveRoundtripTests extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {
  implicit override val generatorDrivenConfig = {
    PropertyCheckConfiguration(minSuccessful = 100)
  }

  private[this] val genLower: Int = -100000
  private[this] val genHigher: Int = -genLower

  implicit val dateTimeGen: Gen[DateTime] = for {
    offset <- Gen.choose(genLower, genHigher)
    time = new DateTime(DateTimeZone.UTC)
  } yield time.plusMillis(offset)

  implicit val javaDateGen: Gen[Date] = dateTimeGen.map(_.toDate)

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

  it should "serialize and deserialize a BigDecimal primitive" in {
    roundtrip[BigDecimal]
  }

  it should "serialize and deserialize a BigInt primitive" in {
    roundtrip[BigInt]
  }
}
