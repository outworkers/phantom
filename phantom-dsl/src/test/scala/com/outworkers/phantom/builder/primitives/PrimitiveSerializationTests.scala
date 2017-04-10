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
import java.util.{Date, UUID, List => JList, Map => JMap, Set => JSet}

import com.datastax.driver.core.{DataType, LocalDate, ProtocolVersion, TypeCodec}
import com.outworkers.phantom.PhantomSuite
import com.outworkers.util.samplers.Sample
import org.joda.time.DateTime
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.Assertion
import org.scalatest.prop.GeneratorDrivenPropertyChecks

import scala.collection.JavaConverters._
import scala.collection.generic.CanBuildFrom

class PrimitiveSerializationTests extends PhantomSuite with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig: PropertyCheckConfiguration = {
    PropertyCheckConfiguration(minSuccessful = 300)
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

  def testCollection[M[X] <: Traversable[X], JType[X], T](
    dataType: DataType,
    gen: Gen[T],
    asJv: M[T] => JType[T]
  )(
    implicit ev: Primitive[T],
    ev2: Primitive[M[T]],
    cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Assertion = {
    val listGen = Gen.buildableOf[M[T], T](gen)
    val codec: TypeCodec[JType[T]] = registry.codecFor(DataType.list(dataType))

    forAll(protocolGen, listGen) { (version: ProtocolVersion, sample: M[T]) =>
      val phantom = ev2.serialize(sample, version)
      val datastax = codec.serialize(asJv(sample), version)
      phantom shouldEqual datastax
    }
  }

  def testMap[K, V](kd: DataType, vd: DataType, gen: Gen[(K, V)])(
    implicit kp: Primitive[K],
    vp: Primitive[V],
    ev2: Primitive[Map[K, V]]
  ): Assertion = {
    val listGen = Gen.mapOf[K, V](gen)
    val codec: TypeCodec[JMap[K, V]] = registry.codecFor(DataType.map(kd, vd))

    forAll(protocolGen, listGen) { (version: ProtocolVersion, sample: Map[K, V]) =>
      val phantom = ev2.serialize(sample, version)
      val datastax = codec.serialize(sample.asJava, version)
      phantom shouldEqual datastax
    }
  }

  def testList[T](dataType: DataType, gen: Gen[T])(
    implicit ev: Primitive[T],
    ev2: Primitive[List[T]]
  ): Assertion = testCollection[List, JList, T](dataType, gen, _.asJava)

  def testSet[T](dataType: DataType, gen: Gen[T])(
    implicit ev: Primitive[T],
    ev2: Primitive[Set[T]]
  ): Assertion = testCollection[Set, JSet, T](dataType, gen, _.asJava)


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

  it should "serialize a java.util.UUID type just like the native codec" in {
    groundtrip[UUID](Gen.uuid, registry.codecFor(DataType.uuid()))
  }

  it should "serialize a TimeUUID type just like the native codec" in {
    groundtrip[UUID](timeuuidGen, registry.codecFor(DataType.timeuuid()))
  }

  it should "serialize a Date type just like the native codec" in {
    groundtrip[Date](javaDateGen, registry.codecFor(DataType.timestamp()))
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

  it should "serialize a List[Long] type just like the native codec" in {
    testList[Long](DataType.bigint(), Arbitrary.arbLong)
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

  it should "serialize a List[java.util.Date] type just like the native codec" in {
    testList[Date](DataType.time(), javaDateGen)
  }

  it should "serialize a List[org.joda.time.DateTime] type just like the native codec" in {
    testList[DateTime](DataType.timestamp(), dateTimeGen)
  }

  it should "serialize a List[TimeUUID] type just like the native codec" in {
    testList[UUID](DataType.timeuuid(), timeuuidGen)
  }

  it should "serialize a List[BigInt] type just like the native codec" in {
    testList[BigInt](DataType.varint(), Arbitrary.arbBigInt)
  }

  it should "serialize a List[BigDecimal] type just like the native codec" in {
    testList[BigDecimal](DataType.decimal(), Arbitrary.arbBigDecimal)
  }

  it should "serialize a Set[String] type just like the native codec" in {
    testSet[String](DataType.text(), Gen.alphaNumStr)
  }

  it should "serialize a Set[Int] type just like the native codec" in {
    testSet[Int](DataType.cint(), Arbitrary.arbInt)
  }

  it should "serialize a Set[Long] type just like the native codec" in {
    testSet[Long](DataType.bigint(), Arbitrary.arbLong)
  }

  it should "serialize a Set[Double] type just like the native codec" in {
    testSet[Double](DataType.cdouble(), Arbitrary.arbDouble)
  }

  it should "serialize a Set[Float] type just like the native codec" in {
    testSet[Float](DataType.cfloat(), Arbitrary.arbFloat)
  }

  it should "serialize a Set[InetAddress] type just like the native codec" in {
    testSet[InetAddress](DataType.inet(), inetAddressGen)
  }

  it should "serialize a Set[UUID] type just like the native codec" in {
    testSet[UUID](DataType.uuid(), Gen.uuid)
  }

  it should "serialize a Set[java.util.Date] type just like the native codec" in {
    testSet[Date](DataType.time(), javaDateGen)
  }

  it should "serialize a Set[org.joda.time.DateTime] type just like the native codec" in {
    testSet[DateTime](DataType.timestamp(), dateTimeGen)
  }

  it should "serialize a Set[TimeUUID] type just like the native codec" in {
    testSet[UUID](DataType.timeuuid(), timeuuidGen)
  }

  it should "serialize a Set[BigInt] type just like the native codec" in {
    testSet[BigInt](DataType.varint(), Arbitrary.arbBigInt)
  }

  it should "serialize a Set[BigDecimal] type just like the native codec" in {
    testSet[BigDecimal](DataType.decimal(), Arbitrary.arbBigDecimal)
  }
}
