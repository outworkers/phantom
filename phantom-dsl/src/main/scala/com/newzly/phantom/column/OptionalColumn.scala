package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import scala.util.Try

abstract class OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T : CassandraPrimitive](table: CassandraTable[Owner, Record]) extends AbstractColumn[Option[T]] {

  type ValueType = Option[T]

  table.addColumn(this)

  def apply(r: Row): Option[T] = optional(r)

  def cassandraType: String = CassandraPrimitive[T].cassandraType

  def optional(r: Row): Option[T] = implicitly[CassandraPrimitive[T]].fromRow(r, name)

  def toCType(v: Option[T]): AnyRef = v map {
    item => implicitly[CassandraPrimitive[T]].toCType(item)
  } getOrElse null
}
