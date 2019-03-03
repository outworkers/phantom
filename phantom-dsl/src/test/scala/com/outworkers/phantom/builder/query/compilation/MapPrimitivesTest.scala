/*
 * Copyright 2013 - 2019 Outworkers Ltd.
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
package com.outworkers.phantom.builder.query.compilation

import com.outworkers.phantom.builder.query.KeySpaceSuite
import org.scalatest.{FlatSpec, Matchers}

class MapPrimitivesTest extends FlatSpec with Matchers with KeySpaceSuite {

  it should "allow defining map columns of custom types if the primitives are supplied through inheritance" in {
    """
    import java.time.Duration
    import com.outworkers.phantom.dsl._
    import com.outworkers.phantom.tables.bugs.{Availability, CustomPrimitives, MyEntity}
    abstract class MapTableBug extends Table[MapTableBug, MyEntity] with CustomPrimitives {

      object key extends StringColumn with PartitionKey
      object timestamp extends DateTimeColumn
      object durationByState extends MapColumn[Availability, Duration]
    }
    """ should compile
  }

  it should "allow defining map columns of custom types if the primitives are supplied through higher scope" in {
    """
    import java.time.Duration
    import com.outworkers.phantom.dsl._
    import com.outworkers.phantom.tables.bugs.{Availability, CustomPrimitives, MyEntity}
    import CustomPrimitives._
    abstract class MapTableBug extends Table[MapTableBug, MyEntity] {

      object key extends StringColumn with PartitionKey
      object timestamp extends DateTimeColumn
      object durationByState extends MapColumn[Availability, Duration]
    }
    """ should compile
  }
}
