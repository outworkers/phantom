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
package com.outworkers.phantom.thrift.tests.compact.suites

import com.datastax.driver.core.ProtocolVersion
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.thrift.util.ThriftTestSuite
import com.outworkers.util.samplers.Sample
import org.scalacheck.Gen
import org.scalatest.{Assertion, FlatSpec}
import org.scalatest.prop.GeneratorDrivenPropertyChecks

class ThriftPrimitivesTest extends FlatSpec with ThriftTestSuite with GeneratorDrivenPropertyChecks {

  val protocolGen: Gen[ProtocolVersion] = Gen.oneOf(ProtocolVersion.values())

  def roundtrip[T : Primitive](gen: Gen[T]): Assertion = {
    val ev = Primitive[T]
    forAll(gen, protocolGen) { (sample, protocol) =>
      ev.deserialize(ev.serialize(sample, protocol), protocol) shouldEqual sample
    }
  }

  def sroundtrip[T : Primitive : Sample]: Assertion = {
    roundtrip[T](Sample.arbitrary[T].arbitrary)
  }

  it should "serialize compact thrift primitives" in {
    import com.outworkers.phantom.thrift.compact._
    sroundtrip[ThriftTest]
  }

  it should "serialize binary thrift primitives" in {
    import com.outworkers.phantom.thrift.binary._
    sroundtrip[ThriftTest]
  }

  it should "serialize lazy binary thrift primitives" in {
    import com.outworkers.phantom.thrift.compact._
    sroundtrip[ThriftTest]
  }

  it should "serialize JSON thrift primitives" in {
    import com.outworkers.phantom.thrift.tjson._
    sroundtrip[ThriftTest]
  }
}
