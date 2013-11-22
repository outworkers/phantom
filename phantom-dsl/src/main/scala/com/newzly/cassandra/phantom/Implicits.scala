/*
 * Copyright 2013 newzly ltd.
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
package com
package newzly
package cassandra
package phantom

object Implicits {

  implicit def columnToQueryColumn[RR: CassandraPrimitive](col: Column[RR]) =
    new QueryColumn(col)

  implicit def simpleColumnToAssignment[RR: CassandraPrimitive](col: AbstractColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def simpleOptionalColumnToAssignment[RR: CassandraPrimitive](col: OptionalColumn[RR]) = {
    new ModifyColumnOptional[RR](col)
  }

  implicit def enumColumnToAssignment[RR <: Enumeration](col: EnumColumn[RR]) = {
    new ModifyColumn[RR#Value](col)
  }

  implicit def jsonColumnToAssignment[RR: Manifest](col: JsonTypeColumn[RR]) = {
    new ModifyColumn[RR](col)
  }

  implicit def seqColumnToAssignment[RR: CassandraPrimitive](col: SeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def jsonSeqColumnToAssignment[RR: Manifest](col: JsonTypeSeqColumn[RR]) = {
    new ModifyColumn[Seq[RR]](col)
  }

  implicit def columnIsSeleCassandraTable[T](col: Column[T]): SelectColumn[T] =
    new SelectColumnRequired[T](col)

  implicit def optionalColumnIsSeleCassandraTable[T](col: OptionalColumn[T]): SelectColumn[Option[T]] =
    new SelectColumnOptional[T](col)
}