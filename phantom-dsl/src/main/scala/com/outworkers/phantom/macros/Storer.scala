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

import com.datastax.driver.core.Row
import com.google.common.base.CaseFormat
import com.outworkers.phantom.CassandraTable
import com.outworkers.phantom.builder.query.InsertQuery
import com.outworkers.phantom.column.AbstractColumn
import com.outworkers.phantom.connectors.KeySpace
import com.outworkers.phantom.keys.{ClusteringOrder, PartitionKey, PrimaryKey}

import scala.collection.immutable.ListMap
import scala.reflect.macros.whitebox

trait Storer[T <: CassandraTable[T, R], R] {
  type Repr

  def store(table: T, input: Repr)(implicit space: KeySpace): InsertQuery.Default[T, R]
}

object Storer {

  def apply[T <: CassandraTable[T, R], R]()(
    implicit ev: Storer[T, R]
  ): Storer[T, R] = ev

  implicit def materialize[T <: CassandraTable[T, R], R]: Storer[T, R] = macro StorerMacro.materialize[T, R]

  type Aux[T <: CassandraTable[T, R], R, Repr0] = Storer[T, R] { type Repr = Repr0 }
}


@macrocompat.bundle
class StorerMacro(override val c: whitebox.Context) extends TableHelperMacro(c) {

  import c.universe._

  def materialize[T : c.WeakTypeTag, R : c.WeakTypeTag]: Tree = {
    val tableType = weakTypeOf[T]
    val rTpe = weakTypeOf[R]
    val refTable = determineReferenceTable(tableType).map(_.typeSignature).getOrElse(tableType)
    val referenceColumns = refTable.decls.sorted.filter(_.typeSignature <:< typeOf[AbstractColumn[_]])
    val tableName = extractTableName(refTable)

    val columns = filterMembers[T, AbstractColumn[_]](exclusions)
    val descriptor = extractor(tableType, rTpe, referenceColumns)
    val clsName = TypeName(c.freshName("anon$"))

    q"""
      final class $clsName extends $macroPkg.Storer[$tableType, $rTpe] {
        type Repr = ${descriptor.storeType}

        ${descriptor.storeMethod}
      }

      new $clsName(): $macroPkg.Storer.Aux[$tableType, $rTpe, ${descriptor.storeType}]
    """
  }
}
