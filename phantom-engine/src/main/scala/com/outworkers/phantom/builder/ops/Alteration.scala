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
package com.outworkers.phantom.builder.ops

import java.nio.ByteBuffer
import java.util.UUID

import org.joda.time.DateTime

/**
  * Typeclass used to prevent invalid type alterations at compile time.
  * Cassandra only allows for specific changes of column types using ALTER Query
  * which means we need to somehow enforce the existing mechanism.
  *
  * We do so by requesting compile time implicit evidence that any type alteration must supply,
  * e.g. that Source and Target have a predefined allowed alteration.
  *
  * The list of allowed ops is found here: [[[https://docs.datastax.com/en/cql/3.1/cql/cql_reference/alter_table_r.html]].
  *
  * @tparam Source The source type of the column.
  * @tparam Target The target type of the column.
  */
sealed trait Alteration[Source, Target]

trait Alterations {
  implicit object IntToVarint extends Alteration[Int, BigInt]

  implicit object TimeUUIDToVarint extends Alteration[UUID, UUID]

  implicit object DoubleToBlob extends Alteration[Double, ByteBuffer]
  implicit object IntToBlob extends Alteration[Int, ByteBuffer]
  implicit object TimestampToBlob extends Alteration[DateTime, ByteBuffer]
  implicit object UUIDToBlob extends Alteration[UUID, ByteBuffer]
}
