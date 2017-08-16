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

import com.outworkers.util.samplers.Sample

case class DerivedField(value: String)

object DerivedField {
  implicit val recordPrimitive: Primitive[DerivedField] = {
    Primitive.derive[DerivedField, String](_.value)(DerivedField.apply)
  }
}

case class DerivedTupleField(value: String, num: Long)

object DerivedTupleField {
  implicit val recordPrimitive: Primitive[DerivedTupleField] = {
    Primitive.derive[DerivedTupleField, (String, Long)](x => x.value -> x.num)(x => {
      val (a, b) = x
      DerivedTupleField(a, b)
    })
  }
}

sealed trait HashStatus
trait Serialized extends HashStatus
trait Unserialized extends HashStatus

sealed case class Username[T <: HashStatus](name: String)

object Username {

  implicit val usernamePrimitive: Primitive[Username[Serialized]] = {
    Primitive.derive[Username[Serialized], String](_.name)(Username.apply)
  }

  implicit val usernameSampler: Sample[Username[Serialized]] = {
    Sample.derive[String, Username[Serialized]](Username.apply)
  }

  def apply(st: String, trigger: Int = 0): Username[Serialized] = new Username[Serialized](st)
}