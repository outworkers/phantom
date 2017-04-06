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
import org.joda.time.{DateTime, DateTimeZone}
import java.net.InetAddress
import java.util.{Date, UUID}
import java.nio.ByteBuffer
import org.scalacheck.{Arbitrary, Gen}
import com.datastax.driver.core.{ CodecUtils, LocalDate, ProtocolVersion }
import scala.collection.generic.CanBuildFrom

class PrimitiveSerializationTests extends FlatSpec
  with Matchers
  with GeneratorDrivenPropertyChecks {

  implicit override val generatorDrivenConfig = {
    PropertyCheckConfiguration(minSuccessful = 300)
  }

  private[this] val protocol = ProtocolVersion.V5
}
