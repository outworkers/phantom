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

import java.math.BigInteger
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

/**
  * A more extensive ScalaCheck based parity check of binary serialization in between
  * Phantom primitives and the underlying Java driver.
  */
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

  def testEmptyCol[M[X] <: Traversable[X], JType[X], T](
    dataType: DataType,
    asJv: M[T] => JType[T]
  )(
    implicit ev: Primitive[T],
    ev2: Primitive[M[T]],
    cbf: CanBuildFrom[Nothing, T, M[T]]
  ): Assertion = {
    val codec: TypeCodec[JType[T]] = registry.codecFor(DataType.list(dataType))
    val empty = cbf().result()

    forAll(protocolGen) { version =>
      val phantom = ev2.serialize(empty, version)
      val datastax = codec.serialize(asJv(empty), version)
      phantom shouldEqual datastax
    }
  }

  def testEmptyList[T](dataType: DataType)(
    implicit ev: Primitive[T],
    ev2: Primitive[List[T]]
  ): Assertion = testEmptyCol[List, JList, T](dataType, _.asJava)

  def testEmptySet[T](dataType: DataType)(
    implicit ev: Primitive[T],
    ev2: Primitive[Set[T]]
  ): Assertion = testEmptyCol[Set, JSet, T](dataType, _.asJava)

  /**
   * Tests a collection type that's a [[Traversable]] with a manually
   * defined corresponding Java collection and Java element type. This is
   * useful for testing roundtrips where the Scala type expoesed as part
   * of the API is not directly equivalent to the Java type used.
   * One such example is [[scala.math.BigDecimal]] and [[java.math.BigDecimal]].
   *
   * @param dataType The DataType as encoded in the Datastax Java Driver.
   * @param gen The ScalaCheck generator of the Scala type.
   * @param asJv A function that converts the Scala collection to the equivalent
   *             Java collection. This comes from [[scala.collection.JavaConverters]].
   * @param conv A function that converts an element with a Scala type to the
   *             corresponding Java type. For instance, converting a [[scala.math.BigDecimal]]
   *             to a [[java.math.BigDecimal]].
   */

  def testCollection[M[X] <: Traversable[X], JType[X], T, JavaType](
    dataType: DataType,
    gen: Gen[T],
    asJv: M[JavaType] => JType[JavaType],
    conv: T => JavaType
  )(
    implicit ev: Primitive[T],
    ev2: Primitive[M[T]],
    cbf: CanBuildFrom[Nothing, T, M[T]],
    cbf2: CanBuildFrom[Nothing, JavaType, M[JavaType]]
  ): Assertion = {
    val listGen = Gen.buildableOf[M[T], T](gen)
    val codec: TypeCodec[JType[JavaType]] = registry.codecFor(DataType.list(dataType))

    forAll(protocolGen, listGen) { (version: ProtocolVersion, sample: M[T]) =>
      val phantom = ev2.serialize(sample, version)

      val javaCol = asJv(sample.map(conv).to[M](cbf2))
      val datastax = codec.serialize(javaCol, version)

      if (!java.util.Arrays.equals(phantom.array(), datastax.array())) {
        info("Comparison between phantom and datastax")
        info(phantom.array().mkString(", "))
        info(datastax.array().mkString(", "))
      }

      phantom shouldEqual datastax
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
      val javaCol = asJv(sample)
      val datastax = codec.serialize(javaCol, version)
      phantom shouldEqual datastax
    }
  }

  def testEmptyMap[K, V](kd: DataType, vd: DataType)(
    implicit kp: Primitive[K],
    vp: Primitive[V],
    ev2: Primitive[Map[K, V]]
  ): Assertion = {
    val sample = Map.empty[K, V]
    val codec: TypeCodec[JMap[K, V]] = registry.codecFor(DataType.map(kd, vd))

    forAll(protocolGen) { version =>
      val phantom = ev2.serialize(sample, version)
      val datastax = codec.serialize(sample.asJava, version)
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
  ): Assertion = {
    testCollection[Set, JSet, T](dataType, gen, _.asJava)
  }

  def testSet[T, InnerType](
    dataType: DataType,
    gen: Gen[T],
    conv: T => InnerType
  )(
    implicit ev: Primitive[T],
    ev2: Primitive[Set[T]]
  ): Assertion = testCollection[Set, JSet, T, InnerType](dataType, gen, _.asJava, conv)


  it should "serialize an empty List[UUID] type just like the native codec" in {
    testEmptyList[UUID](DataType.uuid())
  }

  it should "serialize an empty Set[UUID] type just like the native codec" in {
    testEmptyList[UUID](DataType.uuid())
  }

  it should "serialize an empty Map[String, UUID] type just like the native codec" in {
    testEmptyMap[String, UUID](DataType.text(), DataType.uuid())
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
    testCollection[
      List,
      JList,
      java.util.Date,
      Long
    ](DataType.bigint(), javaDateGen, _.asJava, _.getTime)
  }

  it should "serialize a List[org.joda.time.DateTime] type just like the native codec" in {
    testCollection[
      List,
      JList,
      DateTime,
      Long
    ](DataType.bigint(), dateTimeGen, _.asJava, _.getMillis)
  }

  it should "serialize a List[TimeUUID] type just like the native codec" in {
    testList[UUID](DataType.timeuuid(), timeuuidGen)
  }

  it should "serialize a List[BigInt] type just like the native codec" in {
    testCollection[
      List,
      JList,
      scala.math.BigInt,
      java.math.BigInteger
    ](DataType.varint(), Arbitrary.arbBigInt, _.asJava, _.bigInteger)
  }

  it should "serialize a List[BigDecimal] type just like the native codec" in {
    testCollection[
      List,
      JList,
      scala.math.BigDecimal,
      java.math.BigDecimal
    ](DataType.decimal(), Arbitrary.arbBigDecimal, _.asJava, _.bigDecimal)
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
    testSet[java.util.Date, Long](DataType.bigint(), javaDateGen, _.getTime)
  }

  ignore should "serialize a Set[org.joda.time.DateTime] type just like the native codec" in {
    testSet[DateTime, Long](DataType.bigint(), dateTimeGen, _.getMillis)
  }

  it should "serialize a Set[TimeUUID] type just like the native codec" in {
    testSet[UUID](DataType.timeuuid(), timeuuidGen)
  }

  ignore should "serialize a Set[BigInt] type just like the native codec" in {
    testSet[BigInt, BigInteger](DataType.varint(), Arbitrary.arbBigInt, _.bigInteger)
  }

  ignore should "serialize a Set[BigDecimal] type just like the native codec" in {
    testSet[
      scala.math.BigDecimal,
      java.math.BigDecimal
    ](DataType.decimal(), Arbitrary.arbBigDecimal, _.bigDecimal)
  }

}
