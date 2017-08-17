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

case class MultipleKeyRecord(
  pkey: String,
  int1: Int,
  int2: Int,
  int3: Int,
  int4: Int,
  int5: Int,
  int6: Int,
  int7: Int,
  timestamp: DateTime
)

abstract class MultipleKeys extends Table[MultipleKeys, MultipleKeyRecord] {

  object pkey extends StringColumn with PartitionKey
  object intColumn1 extends IntColumn with PrimaryKey with Index
  object intColumn2 extends IntColumn with PrimaryKey
  object intColumn3 extends IntColumn with PrimaryKey
  object intColumn4 extends IntColumn with PrimaryKey
  object intColumn5 extends IntColumn with PrimaryKey
  object intColumn6 extends IntColumn with PrimaryKey
  object intColumn7 extends IntColumn with PrimaryKey
  object timestamp8 extends DateTimeColumn
}
