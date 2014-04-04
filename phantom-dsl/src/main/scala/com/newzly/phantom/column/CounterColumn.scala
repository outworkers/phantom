package com.newzly.phantom.column

import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

class CounterColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends Column[Owner, Record, Long](table) {
  val cassandraType = "counter"
  val primitive = CassandraPrimitive[Long]
  override val isCounterColumn = true

  def toCType(values: Long): AnyRef = primitive.toCType(values)

  def optional(r: Row): Option[Long] = {
    primitive.fromRow(r, name)
  }

}
