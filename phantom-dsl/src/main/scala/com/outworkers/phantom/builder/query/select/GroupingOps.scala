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
package com.outworkers.phantom.builder.query.select

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.keys.Indexed
import shapeless.{::, HList, HNil}


case class GroupingCondition[Extractor <: HList](
  columns: String
)


trait GroupingOps[Table <: CassandraTable[Table, Record], Record] {

  def apply[A1](
    condition: Table => AbstractColumn[A1] with Indexed
  ): GroupingCondition[A1 :: HNil]

  def apply[A1, A2](
    condition: Table => (AbstractColumn[A1] with Indexed),
    condition2: Table => (AbstractColumn[A2] with Indexed)
  ): GroupingCondition[A2 :: A1 :: HNil]

  def apply[A1, A2, A3](
    condition: Table => AbstractColumn[A1],
    condition2: Table => AbstractColumn[A2],
    condition3: Table => AbstractColumn[A3]
  ): GroupingCondition[A3 :: A2 :: A1 ::  HNil]

}
