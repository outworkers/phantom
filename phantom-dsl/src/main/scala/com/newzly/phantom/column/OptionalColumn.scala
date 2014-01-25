package com.newzly.phantom.column

import com.newzly.phantom.CassandraTable
import com.datastax.driver.core.Row

abstract class OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T](val table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  type ValueType = Option[T]

  override def apply(r: Row) = optional(r)
}
