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
package com.outworkers.phantom.macros

import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.InsertQuery

trait Storer[T <: CassandraTable[T, R], R] {
  type Repr

  def store(table: T, input: Repr): InsertQuery.Default[T, R]
}


object Storer {
  //noinspection ScalaStyle
  type Aux[T <: CassandraTable[T, R], R, Computed] = Storer[T, R] { type Repr = Computed }

  def apply[T <: CassandraTable[T, R], R](
    implicit storer: Storer[T, R]
  ): Aux[T, R, storer.Repr] = storer

  implicit def materializer[T <: CassandraTable[T, R], R]: Storer[T, R] = macro StoreMacro.materialize[T, R]
}