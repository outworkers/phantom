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
package com.outworkers.phantom.builder

import org.joda.time.{DateTime, DateTimeZone}
import java.net.InetAddress
import java.nio.ByteBuffer
import java.util.{Date, UUID}

import com.datastax.driver.core.utils.UUIDs
import org.scalacheck.{Arbitrary, Gen}
import com.datastax.driver.core.{LocalDate, ProtocolVersion}
import com.outworkers.util.samplers._

package object primitives {

  private[this] val genLower: Int = -100000
  private[this] val genHigher: Int = -genLower

  private[this] val inetLowerLimit = 0
  private[this] val inetUpperLimit = 255

  implicit def arbitraryToGen[T](arb: Arbitrary[T]): Gen[T] = arb.arbitrary

  val unicodeChar: Gen[Char] = Gen.oneOf((Char.MinValue to Char.MaxValue).filter(Character.isDefined))

  implicit val strArb: Arbitrary[String] = Sample.arbitrary[String]

  implicit val dateTimeGen: Gen[DateTime] = for {
    offset <- Gen.choose(genLower, genHigher)
    time = new DateTime(DateTimeZone.UTC)
  } yield time.plusMillis(offset)

  def bytebufferGen[T : Primitive : Sample](
    version: ProtocolVersion
  ): Gen[ByteBuffer] = {
    Sample.arbitrary[T].arbitrary.map(
      obj => Primitive[T].serialize(obj, version)
    )
  }

  val protocolGen: Gen[ProtocolVersion] = Gen.oneOf(ProtocolVersion.values())

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

  val timeuuidGen: Gen[UUID] = Gen.delay(UUIDs.timeBased())
}
