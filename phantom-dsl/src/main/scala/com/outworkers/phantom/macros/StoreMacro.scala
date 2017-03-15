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

import scala.reflect.macros.blackbox

@macrocompat.bundle
class StoreMacro(override val c: blackbox.Context) extends RootMacro(c) {
  import c.universe._

  def materialize[T <: CassandraTable[T, R] : c.WeakTypeTag, R : c.WeakTypeTag]: Tree = {

    val tableTpe = weakTypeOf[T]
    val recordType = weakTypeOf[R]

    val storeTpe = typeOf[Unit]

    /*
    q"""
        new $macroPkg.Storer[T, R] {
          override type Repr = $storeTpe
          override def store($tableTerm: $tableTpe): $builderPkg.InsertQuery.Default[$tableTpe, $recordType] = {
          }
        }
    """*/
    EmptyTree
  }
}
