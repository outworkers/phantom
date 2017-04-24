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

case class SchemaBug656Model(id: Int, name: String)

abstract class SchemaBug656Table extends CassandraTable[
  SchemaBug656Table, SchemaBug656Model
] with RootConnector {

  object _1  extends IntColumn(this) with PartitionKey
  object _2  extends StringColumn(this)
  object _3  extends IntColumn(this)
  object _4  extends IntColumn(this)
  object _5  extends StringColumn(this)
  object _6  extends StringColumn(this)
  object _7 extends IntColumn(this)
  object _8 extends IntColumn(this)
  object _9 extends IntColumn(this)
  object _10 extends StringColumn(this)
  object _11 extends StringColumn(this)
  object _12 extends StringColumn(this)
  object _13 extends StringColumn(this)
  object _14 extends IntColumn(this)
  object _15 extends IntColumn(this)
  object _16 extends IntColumn(this)
  object _17 extends IntColumn(this)
  object _18 extends IntColumn(this)
  object _20 extends IntColumn(this)
  object _21 extends StringColumn(this)
  object _22 extends StringColumn(this)
  object _23 extends StringColumn(this)
  object _24 extends StringColumn(this)
  object _25 extends StringColumn(this)
}
