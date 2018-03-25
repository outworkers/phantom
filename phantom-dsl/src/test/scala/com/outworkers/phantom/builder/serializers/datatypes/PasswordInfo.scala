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
package com.outworkers.phantom.builder.serializers.datatypes

import com.outworkers.phantom.builder.primitives.Primitive

/**
  * The password details.
  *
  * @param hasher The ID of the hasher used to hash this password.
  * @param password The hashed password.
  * @param salt The optional salt used when hashing.
  */
case class PasswordInfo(
  hasher: String,
  password: String,
  salt: Option[String] = None
)

object PasswordInfo{
  implicit val conversion: Primitive[PasswordInfo] = {
    Primitive.derive[PasswordInfo, (String, String, Option[String])](pi => (pi.hasher, pi.password, pi.salt)) { x =>
      val (a, b, c) = x
      PasswordInfo(a, b, c)
    }
  }
}
