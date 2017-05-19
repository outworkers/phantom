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
package com.outworkers.phantom.column

import java.util.UUID

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.builder.syntax.CQLSyntax

class TimeUUIDColumn[
  T <: CassandraTable[T, R],
  R
](table: CassandraTable[T, R])(
  implicit ev: Primitive[UUID]
) extends PrimitiveColumn[T, R, UUID](table) {
  override val cassandraType = CQLSyntax.Types.TimeUUID
}