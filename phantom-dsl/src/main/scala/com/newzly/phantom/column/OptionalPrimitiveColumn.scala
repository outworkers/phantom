package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

@implicitNotFound(msg = "Type ${T} must be a Cassandra primitive")
class OptionalPrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, T: CassandraPrimitive](t: CassandraTable[Owner, Record]) extends OptionalColumn[Owner, Record, T](t) {
  def toCType(v: T): AnyRef = CassandraPrimitive[T].toCType(v)
  def cassandraType: String = CassandraPrimitive[T].cassandraType
  def optional(r: Row): Option[T] = implicitly[CassandraPrimitive[T]].fromRow(r, name)
}
