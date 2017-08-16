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
package com.outworkers.phantom.tables

import com.outworkers.phantom.dsl._
import org.joda.time.LocalDate

case class PrimitiveCassandra22(
  pkey: String,
  short: Short,
  byte: Byte,
  date: LocalDate
)

abstract class PrimitivesCassandra22 extends Table[
  PrimitivesCassandra22,
  PrimitiveCassandra22
] {

  object pkey extends StringColumn with PartitionKey

  object short extends SmallIntColumn

  object byte extends TinyIntColumn

  object date extends LocalDateColumn
}
