package com.newzly.phantom.column

import scala.annotation.implicitNotFound
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import com.datastax.driver.core.Row

@implicitNotFound(msg = "Type ${T} must be a Cassandra primitive")
class OptionalPrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, @specialized(Int, Double, Float, Long, Boolean, Short) T : CassandraPrimitive](t: CassandraTable[Owner, Record]) extends OptionalColumn[Owner, Record, T](t) {

}
