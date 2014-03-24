package com.newzly.phantom.column

import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

class CounterColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends AbstractColumn[Long] {
  val cassandraType = s"${CassandraPrimitive[Long].cassandraType}"
  val primitive = CassandraPrimitive[Long]

  def toCType(values: Long): AnyRef = primitive.toCType(values)

  def apply(r: Row): Long = {
    optional(r).getOrElse(null.asInstanceOf[Long])
  }

  def optional(r: Row): Option[Long] = {
    primitive.fromRow(r, name)
  }

}
