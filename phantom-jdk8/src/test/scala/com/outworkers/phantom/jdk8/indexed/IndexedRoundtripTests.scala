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
package com.outworkers.phantom.jdk8.indexed

import java.time.ZoneOffset

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.jdk8.tables._
import org.scalacheck.Gen
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import org.scalatest.{Assertion, FlatSpec, Matchers}

class IndexedRoundtripTests extends FlatSpec with Matchers with GeneratorDrivenPropertyChecks {

  private[this] val genLower: Int = -100000
  private[this] val genHigher: Int = -genLower

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }


  def roundtrip[T](gen: Gen[T])(fn: T => T)(implicit ev: Primitive[T]): Assertion = {
    forAll(gen, protocolGen) { (sample, version) =>
      ev.deserialize(ev.serialize(sample, version), version) shouldEqual fn(sample)
    }
  }

  it should "serialize and deserialize ZonedDateTime instances using indexed primitives" in {
    roundtrip(zonedDateTimeGen)(_.withZoneSameInstant(ZoneOffset.UTC))
  }

  it should "serialize and deserialize OffsetDateTime instances using indexed primitives" in {
    roundtrip(offsetDateTimeGen)(_.withOffsetSameInstant(ZoneOffset.UTC))
  }

  it should "serialize and deserialize LocalDate instances using indexed primitives" in {
    roundtrip(localDateGen)(identity)
  }

  it should "serialize and deserialize LocalDateTime instances using indexed primitives" in {
    roundtrip(localDateTimeGen)(identity)
  }
}