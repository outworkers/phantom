package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.{CassandraPrimitive, CassandraTable}
import scala.util.Try

abstract class OptionalColumn[Owner <: CassandraTable[Owner, Record], Record, T](table: CassandraTable[Owner, Record]) extends AbstractColumn[Option[T]] {

  def apply(r: Row): Option[T] = optional(r)

  def cassandraType: String

  def optional(r: Row): Option[T]
}
