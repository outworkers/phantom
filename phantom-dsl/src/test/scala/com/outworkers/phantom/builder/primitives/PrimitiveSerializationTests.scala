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

import com.datastax.driver.core.{DataType, LocalDate, ProtocolVersion, TypeCodec}
import com.outworkers.phantom.PhantomSuite
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertion
import org.scalatest.prop.GeneratorDrivenPropertyChecks
import java.util.{Date, UUID, List => JList, Map => JMap, Set => JSet}

import com.google.common.base.Charsets
import com.outworkers.util.samplers.Sample

import scala.collection.JavaConverters._

class PrimitiveSerializationTests extends PhantomSuite with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  private[this] def showBuffer(buffer: ByteBuffer): String = {
    new String(buffer.array(), Charsets.UTF_8)
  }

  val registry = session.getCluster.getConfiguration.getCodecRegistry

  def roundtrip[T : Primitive : Arbitrary](codec: TypeCodec[T]): Assertion = {
    val ev = Primitive[T]
    forAll { (version: ProtocolVersion, sample: T) =>
      ev.serialize(sample, version) shouldEqual codec.serialize(sample, version)
    }
  }

  def roundtrip[T : Primitive : Arbitrary, DriverType](
    codec: TypeCodec[DriverType],
    conv: T => DriverType
  ): Assertion = {
    val ev = Primitive[T]
    forAll { (version: ProtocolVersion, sample: T) =>
      ev.serialize(sample, version) shouldEqual codec.serialize(conv(sample), version)
    }
  }

  def bufferTest[T : Primitive : Sample]: Assertion = {

    val ev = Primitive[ByteBuffer]
    val codec: TypeCodec[ByteBuffer] = registry.codecFor(DataType.blob())

    val customGen = for {
      version <- protocolGen
      buffer <- bytebufferGen[T](version)
    } yield version -> buffer

    forAll(customGen) { case (v, bf) =>
      ev.serialize(bf, v) shouldEqual codec.serialize(bf, v)
    }
  }

  def groundtrip[T](
    gen: Gen[T],
    codec: TypeCodec[T]
  )(implicit ev: Primitive[T]): Assertion = {
    forAll(protocolGen, gen) { (version: ProtocolVersion, sample: T) =>
      ev.serialize(sample, version) shouldEqual codec.serialize(sample, version)
    }
  }

  def testList[T](dataType: DataType, gen: Gen[T])(
    implicit ev: Primitive[T],
    ev2: Primitive[List[T]]
  ): Assertion = {
    val listGen = Gen.listOf(gen)
    val codec: TypeCodec[JList[T]] = registry.codecFor(DataType.list(dataType))

    forAll(protocolGen, listGen) { (version: ProtocolVersion, sample: List[T]) =>
      val phantom = ev2.serialize(sample, version)
      val datastax = codec.serialize(sample.asJava, version)
      phantom shouldEqual datastax
    }
  }

  it should "serialize a Byte type just like the native codec" in {
    roundtrip[Byte](registry.codecFor(DataType.tinyint()))
  }

  it should "serialize a Short type just like the native codec" in {
    roundtrip[Short](registry.codecFor(DataType.smallint()))
  }

  it should "serialize a Boolean type just like the native codec" in {
    roundtrip[Boolean](registry.codecFor(DataType.cboolean()))
  }

  it should "serialize a Double type just like the native codec" in {
    roundtrip[Double](registry.codecFor(DataType.cdouble()))
  }

  it should "serialize a Int type just like the native codec" in {
    roundtrip[Int](registry.codecFor(DataType.cint()))
  }

  it should "serialize a Float type just like the native codec" in {
    roundtrip[Float](registry.codecFor(DataType.cfloat()))
  }

  it should "serialize a Long type just like the native codec" in {
    roundtrip[Long](registry.codecFor(DataType.bigint()))
  }

  it should "serialize a BigInt type just like the native codec" in {
    roundtrip[BigInt, java.math.BigInteger](registry.codecFor(DataType.varint()), _.bigInteger)
  }

  it should "serialize a BigDecimal type just like the native codec" in {
    roundtrip[BigDecimal, java.math.BigDecimal](registry.codecFor(DataType.decimal()), _.bigDecimal)
  }

  it should "serialize a String type just like the native codec" in {
    roundtrip[String](registry.codecFor(DataType.text()))
  }

  it should "serialize a String type just like the varchar codec" in {
    roundtrip[String](registry.codecFor(DataType.varchar()))
  }

  it should "serialize a java.util.Date type just like the native codec" in {
    groundtrip[UUID](Gen.uuid, registry.codecFor(DataType.uuid()))
  }

  it should "serialize a TimeUUID type just like the native codec" in {
    groundtrip[UUID](timeuuidGen, registry.codecFor(DataType.timeuuid()))
  }

  it should "serialize a Date type just like the native codec" in {
    groundtrip[Date](javaDateGen, registry.codecFor(DataType.date()))
  }

  it should "serialize a UUID type just like the native codec" in {
    groundtrip[LocalDate](localDateGen, registry.codecFor(DataType.date()))
  }

  it should "serialize a ByteBuffer type just like the native codec" in {
    bufferTest[Int]
  }

  it should "serialize a List[String] type just like the native codec" in {
    testList[String](DataType.text(), Gen.alphaNumStr)
  }

  it should "serialize a List[Int] type just like the native codec" in {
    testList[Int](DataType.cint(), Arbitrary.arbInt)
  }
  it should "serialize a List[Double] type just like the native codec" in {
    testList[Double](DataType.cdouble(), Arbitrary.arbDouble)
  }

  it should "serialize a List[Float] type just like the native codec" in {
    testList[Float](DataType.cfloat(), Arbitrary.arbFloat)
  }

  it should "serialize a List[InetAddress] type just like the native codec" in {
    testList[InetAddress](DataType.inet(), inetAddressGen)
  }

  it should "serialize a List[UUID] type just like the native codec" in {
    testList[UUID](DataType.uuid(), Gen.uuid)
  }

  it should "serialize a List[TimeUUID] type just like the native codec" in {
    testList[UUID](DataType.timeuuid(), timeuuidGen)
  }

  it should "serialize a List[BigDecimal] type just like the native codec" in {
    testList[BigDecimal](DataType.decimal(), Arbitrary.arbBigDecimal)
  }
}
