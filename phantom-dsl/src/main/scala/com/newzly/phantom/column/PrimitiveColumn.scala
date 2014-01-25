package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, @specialized(Int, Double, Float, Long) RR: CassandraPrimitive](override val table: CassandraTable[Owner, Record]) extends Column[Owner, Record, RR](table) {

  def cassandraType: String = CassandraPrimitive[RR].cassandraType
  def toCType(v: RR): AnyRef = CassandraPrimitive[RR].toCType(v)

  def optional(r: Row): Option[RR] =
    implicitly[CassandraPrimitive[RR]].fromRow(r, name)
}
