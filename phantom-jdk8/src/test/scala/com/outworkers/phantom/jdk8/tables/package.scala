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
package com.outworkers.phantom.jdk8

import java.time._

import com.outworkers.util.samplers._
import org.scalacheck.Arbitrary

import scala.collection.JavaConverters._
import scala.util.Random

package object tables {

  implicit object ZoneIdSampler extends Sample[ZoneId] {
    override def sample: ZoneId = ZoneId.of(Generators.oneOf(ZoneId.getAvailableZoneIds.asScala.toSeq))
  }

  implicit object InstantSampler extends Sample[Instant] {
    private[this] val sampling = 150000

    override def sample: Instant = {
      val now = Instant.now().toEpochMilli
      val offset = Random.nextInt(sampling)
      val direction = Random.nextBoolean()
      Instant.ofEpochMilli(if (direction) now + offset else now - offset)
    }
  }

  implicit object OffsetDateTimeSampler extends Sample[OffsetDateTime] {
    override def sample: OffsetDateTime = OffsetDateTime.ofInstant(gen[Instant], gen[ZoneId])
  }

  implicit object ZonedDateTimeSampler extends Sample[ZonedDateTime] {
    override def sample: ZonedDateTime = ZonedDateTime.ofInstant(gen[Instant], gen[ZoneId])
  }

  implicit val primitivesJdk8Gen: Arbitrary[Jdk8Row] = Sample.arbitrary[Jdk8Row]
  implicit val optionalPrimitivesJdk8Gen: Arbitrary[OptionalJdk8Row] = Sample.arbitrary[OptionalJdk8Row]

  implicit object Jdk8RowSampler extends Sample[Jdk8Row] {
    private[this] val sampling = 150
    def sample: Jdk8Row = {
      Jdk8Row(
        pkey = gen[String],
        offsetDateTime = gen[OffsetDateTime],
        zonedDateTime = gen[ZonedDateTime],
        localDate = LocalDate.now().plusDays(Random.nextInt(sampling)),
        localDateTime = LocalDateTime.now().plusSeconds(Random.nextInt(sampling))
      )
    }
  }

  implicit object OptionalJdk8RowSampler extends Sample[OptionalJdk8Row] {
    private[this] val sampling = 150
    def sample: OptionalJdk8Row = {
      OptionalJdk8Row(
        pkey = gen[String],
        offsetDateTime = genOpt[OffsetDateTime],
        zonedDateTime = genOpt[ZonedDateTime],
        localDate = Some(LocalDate.now().plusDays(Random.nextInt(sampling).toLong)),
        localDateTime = Some(LocalDateTime.now().plusSeconds(Random.nextInt(sampling).toLong))
      )
    }
  }

}
