package com.websudos.phantom.builder.ops

import com.datastax.driver.core.Row
import com.websudos.phantom.CassandraTable
import com.websudos.phantom.builder.QueryBuilder
import com.websudos.phantom.column._
import com.websudos.phantom.keys._
import shapeless.<:!<

import scala.annotation.implicitNotFound

private[phantom] abstract class AbstractModifyColumn[RR](col: AbstractColumn[RR]) {

  def setTo(value: RR): UpdateClause.Condition = new UpdateClause.Condition(QueryBuilder.Update.set(col.name, col.asCql(value)))
}

sealed class ModifyColumn[RR](col: AbstractColumn[RR]) extends AbstractModifyColumn[RR](col)

sealed class ModifyColumnOptional[Owner <: CassandraTable[Owner, Record], Record, RR](col: OptionalColumn[Owner, Record, RR])
  extends AbstractModifyColumn[Option[RR]](col)

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
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.valueAsCql(value)))
    }

    def prependAll(values: List[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.prepend(col.name, col.asCql(values)))
    }

    def append(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.valueAsCql(value)))
    }

    def appendAll(values: List[RR]): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.append(col.name, col.asCql(values)))
    }

    def discard(value: RR): UpdateClause.Condition = {
      new UpdateClause.Condition(QueryBuilder.Collections.discard(col.name, col.valueAsCql(value)))
    }

    def discardAll(values: List[RR]): UpdateClause.Condition = {
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
      new UpdateClause.Condition(QueryBuilder.Collections.put(col.name, Tuple2(col.keyAsCql(value._1).toString, col.valueAsCql(value._2))))
    }

    def putAll[L](values: L)(implicit ev1: L => Traversable[(A, B)]): UpdateClause.Condition = {
      new UpdateClause.Condition(
        QueryBuilder.Collections.put(col.name, values.map(item => {
          Tuple2(col.keyAsCql(item._1).toString, col.valueAsCql(item._2).toString)
        }).toSeq : _*))
    }
  }
}

sealed class ModifiableColumn[T]

private[ops] trait ModifyMechanism extends CollectionOperators with ColumnModifiers {

  @implicitNotFound(msg = "This type of column can not be modified. Indexes are fixed, counters can only be incremented and decremented.")
  implicit def columnToModifyColumn[RR](col: AbstractColumn[RR])
                                       (implicit ev: col.type <:!< Unmodifiable,
                                         ev2: col.type <:!< CollectionValueDefinition[RR]): ModifyColumn[RR] = new ModifyColumn(col)


}


