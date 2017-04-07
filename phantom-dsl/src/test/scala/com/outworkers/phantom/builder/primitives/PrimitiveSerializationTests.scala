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

import com.datastax.driver.core.{DataType, ProtocolVersion, TypeCodec}
import com.outworkers.phantom.PhantomSuite
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertion
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import java.util.{UUID, List => JList, Map => JMap, Set => JSet}

import scala.collection.JavaConverters._

class PrimitiveSerializationTests extends PhantomSuite with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  val registry = session.getCluster.getConfiguration.getCodecRegistry

  private[this] val protocol = ProtocolVersion.V5

  def roundtrip[T : Primitive : Arbitrary](codec: TypeCodec[T], version: ProtocolVersion): Assertion = {
    val ev = Primitive[T]
    forAll { sample: T =>
      ev.serialize(sample, version) shouldEqual codec.serialize(sample, version)
    }
  }

  def groundtrip[T](
    gen: Gen[T],
    codec: TypeCodec[T],
    version: ProtocolVersion
  )(implicit ev: Primitive[T]): Assertion = forAll(gen) { sample: T =>
    ev.serialize(sample, version) shouldEqual codec.serialize(sample, version)
  }

  it should "serialize a Boolean type just like the native codec" in {
    roundtrip[Boolean](registry.codecFor(DataType.cboolean()), protocol)
  }

  it should "serialize a Float type just like the native codec" in {
    roundtrip[Float](registry.codecFor(DataType.cfloat()), protocol)
  }

  it should "serialize a Long type just like the native codec" in {
    roundtrip[Long](registry.codecFor(DataType.bigint()), protocol)
  }

  it should "serialize a Double type just like the native codec" in {
    roundtrip[Double](registry.codecFor(DataType.cdouble()), protocol)
  }

  it should "serialize a String type just like the native codec" in {
    roundtrip[String](registry.codecFor(DataType.text()), protocol)
  }

  it should "serialize a String type just like the varchar codec" in {
    roundtrip[String](registry.codecFor(DataType.text()), protocol)
  }

  it should "serialize a UUID type just like the native codec" in {
    groundtrip[UUID](Gen.uuid, registry.codecFor(DataType.uuid()), protocol)
  }

  it should "serialize a TimeUUID type just like the native codec" in {
    groundtrip[UUID](timeuuidGen, registry.codecFor(DataType.timeuuid()), protocol)
  }

  it should "serialize a List[String] type just like the native codec" in {
    val gen = Gen.listOf(Gen.alphaNumStr)
    val ev = Primitive[List[String]]
    val codec: TypeCodec[JList[String]] = registry.codecFor(DataType.list(DataType.text()))

    forAll(gen) { sample: List[String] =>
      ev.serialize(sample, protocol) shouldEqual codec.serialize(sample.asJava, protocol)
    }
  }

}
