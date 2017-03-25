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
import com.outworkers.phantom.dsl.Row

import scala.reflect.macros.blackbox


trait SelectClause[Input] {
  type Repr

  def extractor[T <: CassandraTable[T, _]](functions: Input, table: T): Row => Repr

  def names[T <: CassandraTable[T, _]](functions: Input): List[String]
}

object SelectClause {

  type Aux[Input, Output] = SelectClause[Input] { type Repr = Output }
}

@macrocompat.bundle
class SelectMacro(override val c: blackbox.Context) extends RootMacro(c) {

  import c.universe._

  protected[this] val functionTerm = q"functions"

  def materialize[In : c.WeakTypeTag]: Tree = {
    val in = weakTypeOf[In]

    Console.println(s"Input type is :${printType(in)}")
    Console.println(s"Type args of input type: ${in.typeArgs.map(printType).mkString(", ")}")

    val repr = in.typeArgs.map(_.typeArgs(1))
    val tableTpe = in.typeArgs.head.typeArgs.head
    val finalTpe = tq"(..$repr)"
    val counter = in.typeArgs.size

    val appliers = for (i <- 1 to counter) yield q"$functionTerm.${tupleTerm(i)}.apply($tableTerm)"
    val columnNames = appliers.map(t => q"$t.col.name")
    val extractors = appliers.map(t => q"$t.apply($rowTerm)")

    q"""
        new com.outworkers.phantom.macros.SelectClause[$in] {
          type Repr = $finalTpe

          def extractor[$tableTpe]($functionTerm: $in, $tableTerm: $tableTpe): $rowType => $finalTpe = {
            $rowTerm: $rowType => ..$extractors
          }

          def names[$tableTpe]($functionTerm: $in): _root_.scala.collection.immutable.List[$strTpe] = {
            List.apply(..$columnNames)
          }
        }
    """
  }
}