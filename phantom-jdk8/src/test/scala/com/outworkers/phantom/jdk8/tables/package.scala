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

import com.outworkers.util.testing.{Sample, _}

import scala.util.Random

package object tables {

  implicit object Jdk8RowSampler extends Sample[Jdk8Row] {
    private[this] val sampling = 150
    def sample: Jdk8Row = {
      Jdk8Row(
        gen[String],
        OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(Random.nextInt(sampling).toLong),
        ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(Random.nextInt(sampling).toLong),
        LocalDate.now().plusDays(Random.nextInt(sampling)),
        LocalDateTime.now().plusSeconds(Random.nextInt(sampling))
      )
    }
  }

  implicit object OptionalJdk8RowSampler extends Sample[OptionalJdk8Row] {
    private[this] val sampling = 150
    def sample: OptionalJdk8Row = {
      OptionalJdk8Row(
        gen[String],
        Some(OffsetDateTime.now(ZoneOffset.UTC).plusSeconds(Random.nextInt(sampling).toLong)),
        Some(ZonedDateTime.now(ZoneOffset.UTC).plusSeconds(Random.nextInt(sampling).toLong)),
        Some(LocalDate.now().plusDays(Random.nextInt(sampling).toLong)),
        Some(LocalDateTime.now().plusSeconds(Random.nextInt(sampling).toLong))
      )
    }
  }

}
