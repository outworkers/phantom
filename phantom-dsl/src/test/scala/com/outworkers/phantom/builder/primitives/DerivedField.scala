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

