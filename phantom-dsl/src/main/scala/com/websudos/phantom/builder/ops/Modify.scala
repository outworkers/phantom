/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Outworkers Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.websudos.phantom.builder.ops

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.builder.clauses.{PreparedWhereClause, UpdateClause}
import com.websudos.phantom.builder.query.CQLQuery
import com.websudos.phantom.builder.query.prepared.PrepareMark
import com.websudos.phantom.column._
import com.websudos.phantom.keys._
import shapeless.<:!<

import scala.annotation.implicitNotFound

private[phantom] abstract class AbstractModifyColumn[RR](col: AbstractColumn[RR]) {

  /**
    * Default setTo clause for all update queries except for map columns.
    * All setTo operations from the DSL will be serialized through a modify column
    * through an implicit conversion at the DSL level.
    *
    * Map columns have a different implicits that take precedence over ModifyColumn
    * to allow for better support of map updates.
    *
    * @param value The typed value to set the column to.
    * @return A serialized update clause condition that is latter appended to the Set Query part of an update query.
    */
  def setTo(value: RR): UpdateClause.Condition = {
    new UpdateClause.Condition(QueryBuilder.Update.setTo(col.name, col.asCql(value)))
  }

  /**
    * Set to method used to support prepared modify queries.
    * It will only accept prepared mark arguments, where phantom provides only one global instance called "?"
    * to match the CQL syntax expected by the end user.
    * Example:
    * {{{
    *   database.table.column.update.p_modify(_.bla setTo ?).where(_.a eqs ?)
    * }}}
    *
    * @param value The prepare mark value to set this to. This is just provided for consnistency with natural CQL.
    * @return The prepared setTo clause part that gets appended to the Set Part of the update query.
    */
  def setTo(value: PrepareMark): PreparedWhereClause.ParametricCondition[RR] = {
    new PreparedWhereClause.ParametricCondition[RR](
      QueryBuilder.Update.setTo(col.name, value.qb.queryString)
    )
  }
}

sealed class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col)

sealed class ModifyColumnOptional[RR](col: OptionalColumn[_, _, RR])
  extends AbstractModifyColumn[Option[RR]](col) {

  /**
    * Default setTo clause for all update queries except for map columns.
    * All setTo operations from the DSL will be serialized through a modify column
    * through an implicit conversion at the DSL level.
    *
    * Map columns have a different implicits that take precedence over ModifyColumn
    * to allow for better support of map updates.
    *
    * @param value The typed value to set the column to.
    * @return A serialized update clause condition that is latter appended to the Set Query part of an update query.
    */
  def setIfDefined(value: Option[RR]): UpdateClause.Condition = {
    value match {
      case Some(existing) => new UpdateClause.Condition(QueryBuilder.Update.setTo(col.name, col.asCql(value)))
      case None => new UpdateClause.Condition(qb = CQLQuery.empty, skipped = true)
    }
  }

}

abstract class SelectColumn[T](val col: AbstractColumn[_]) {
  def apply(r: Row): T
}

sealed trait ColumnModifiers {

  implicit class SelectColumnRequired[Owner <: CassandraTable[Owner, Record], Record, T](col: Column[Owner, Record, T]) extends SelectColumn[T](col) {
    def apply(r: Row): T = col.apply(r)
  }

  implicit class SelectColumnOptional[Owner <: CassandraTable[Owner, Record], Record, T](col: OptionalColumn[Owner, Record, T])
    extends SelectColumn[Option[T]](col) {
    def apply(r: Row): Option[T] = col.apply(r)
  }
}

trait CollectionOperators {

  implicit class ListLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractListColumn[Owner, Record, RR]) {

    def prepend(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.asCql(value :: Nil)))
    }

    def prepend(values: List[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.asCql(values)))
    }

    def append(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.asCql(value :: Nil)))
    }

    def append(values: List[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.asCql(values)))
    }

    def discard(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.discard(col.name, col.asCql(value :: Nil)))
    }

    def discard(values: List[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.discard(col.name, col.asCql(values)))
    }

    def setIdx(i: Int, value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.setIdX(col.name, i.toString, col.valueAsCql(value)))
    }
  }

  implicit class SetLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, RR](col: AbstractSetColumn[Owner, Record, RR]) {

    def add(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.add(col.name, Set(col.valueAsCql(value))))
    }

    def addAll(values: Set[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.add(col.name, values.map(col.valueAsCql)))
    }

    def remove(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.remove(col.name, Set(col.valueAsCql(value))))
    }

    def removeAll(values: Set[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.remove(col.name, values.map(col.valueAsCql)))
    }
  }

  implicit class MapLikeModifyColumn[Owner <: CassandraTable[Owner, Record], Record, A, B](col: AbstractMapColumn[Owner, Record, A, B]) {

    def set(key: A, value: B): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.mapSet(col.name, col.keyAsCql(key).toString, col.valueAsCql(value)))
    }

    def put(value: (A, B)): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.put(
        col.name,
        col.keyAsCql(value._1).toString -> col.valueAsCql(value._2))
      )
    }

    def putAll[L](values: L)(implicit ev1: L => Traversable[(A, B)]): UpdateClause.Condition = {
      new UpdateClause.Condition(
        QueryBuilder.Collections.put(col.name, values.map { case (key, value) => {
          Tuple2(col.keyAsCql(key).toString, col.valueAsCql(value).toString)
        }}.toSeq : _*))
    }
  }
}

sealed class ModifiableColumn[T]

private[ops] trait ModifyMechanism extends CollectionOperators with ColumnModifiers {

  @implicitNotFound(msg = "This type of column can not be modified." +
    "Indexes are fixed, counters can only be incremented and decremented.")
  implicit def columnToModifyColumn[
    RR
  ](col: AbstractColumn[RR])(
    implicit ev: col.type <:!< Unmodifiable,
    ev2: col.type <:!< CollectionValueDefinition[RR]
  ): ModifyColumn[RR] = new ModifyColumn(col)

  @implicitNotFound(msg = "This type of column can not be modified." +
    "Indexes are fixed, counters can only be incremented and decremented.")
  implicit def optionalColumnToModifyColumn[
    Table <: CassandraTable[Table, Rec],
    Rec,
    RR
  ](
    col: OptionalColumn[Table, Rec, RR]
  )(implicit ev: col.type <:!< Unmodifiable,
      ev2: col.type <:!< CollectionValueDefinition[RR]
  ): ModifyColumnOptional[RR] = new ModifyColumnOptional(col)



}


