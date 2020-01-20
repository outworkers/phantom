/*
 * Copyright 2013 - 2020 Outworkers Ltd.
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
package com.outworkers.phantom.tables.bugs

import java.time.Duration

import com.outworkers.phantom.dsl._

sealed trait Availability

object Availability {
  case object Available extends Availability
  case object Pending extends Availability
  case object UnAvailable extends Availability

  def apply(name: String): Availability = fromString(name)

  def fromString(name: String): Availability = name match {
    case "Available" => Available
    case "Pending" => Pending
    case "UnAvailable" => UnAvailable
    case other => throw new IllegalArgumentException(s"$other is not a valid type of Availability")
  }

}

trait CustomPrimitives {

  implicit val availabilityPrimitive: Primitive[Availability] = {
    Primitive.derive[Availability, String](_.toString)(Availability.apply)
  }
  implicit val durationPrimitive: Primitive[Duration] = {
    Primitive.derive[Duration, String](_.toString)(Duration.parse)
  }
}

object CustomPrimitives extends CustomPrimitives

case class MyEntity(
  key: String,
  timestamp: DateTime,
  durationByState: Map[Availability, Duration]
)


abstract class MapTableBug extends Table[MapTableBug, MyEntity] with CustomPrimitives {
  object key extends StringColumn with PartitionKey
  object timestamp extends DateTimeColumn
  object durationByState extends MapColumn[Availability, Duration]
}


import CustomPrimitives._

abstract class MapTableBug2 extends Table[MapTableBug, MyEntity] {

  object key extends StringColumn with PartitionKey
  object timestamp extends DateTimeColumn
  object durationByState extends MapColumn[Availability, Duration]
}
