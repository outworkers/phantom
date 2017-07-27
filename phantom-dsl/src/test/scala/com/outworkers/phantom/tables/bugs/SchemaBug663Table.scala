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
package com.outworkers.phantom.tables.bugs

import com.outworkers.phantom.dsl._

sealed trait SchemaRecord663
case class SchemaBug663A(a: Int) extends SchemaRecord663
case class SchemaBug663B(b: Int) extends SchemaRecord663

abstract class SchemaBug663Table extends Table[SchemaBug663Table, SchemaRecord663] {
  object discriminator extends IntColumn with PartitionKey
  object a extends IntColumn
  object b extends IntColumn

  override def fromRow(row: Row): SchemaRecord663 = {
    if (discriminator(row) % 2 == 0) {
      SchemaBug663A(a(row))
    } else {
      SchemaBug663B(b(row))
    }
  }
}