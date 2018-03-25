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

import java.util.UUID
import com.outworkers.phantom.dsl._
import com.outworkers.phantom.builder.serializers.datatypes.PasswordInfo

case class TuplePartitionRecord(
  id: PasswordInfo,
  rec: UUID,
  props: Map[String, String]
)

abstract class TuplePartitionTable extends Table[TuplePartitionTable, TuplePartitionRecord] {
  object id extends Col[PasswordInfo] with PartitionKey
  object rec extends Col[UUID]
  object props extends Col[Map[String, String]]
}
