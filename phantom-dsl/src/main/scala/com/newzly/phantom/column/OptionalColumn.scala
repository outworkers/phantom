package com.newzly.phantom.column

import com.newzly.phantom.CassandraTable
import com.datastax.driver.core.Row

abstract class OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T](table: CassandraTable[Owner, Record]) extends AbstractColumn[T] {

  table.addColumn(this)

  def apply(r: Row): Option[T] = optional(r)
}
