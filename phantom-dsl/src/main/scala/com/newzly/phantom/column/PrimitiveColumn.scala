package com.newzly.phantom.column

import java.util.Date
import scala.annotation.implicitNotFound
import org.joda.time.DateTime
import com.datastax.driver.core.Row
import com.newzly.phantom.{ CassandraPrimitive, CassandraTable }

@implicitNotFound(msg = "Type ${RR} must be a Cassandra primitive")
class PrimitiveColumn[Owner <: CassandraTable[Owner, Record], Record, @specialized(Int, Double, Float, Long) RR: CassandraPrimitive](t: CassandraTable[Owner, Record]) extends Column[Owner, Record, RR](t) {

  getTable.addColumn(this)

  def cassandraType: String = CassandraPrimitive[RR].cassandraType
  def toCType(v: RR): AnyRef = CassandraPrimitive[RR].toCType(v)

  def optional(r: Row): Option[RR] =
    implicitly[CassandraPrimitive[RR]].fromRow(r, name)
}

class TimeSeries[T]

/**
 * A Date Column, used to enforce restrictions on clustering order.
 * @param table The Cassandra Table to which the column belongs to.
 * @tparam Owner The Owner of the Record.
 * @tparam Record The Record type.
 */
class DateColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends PrimitiveColumn[Owner, Record, Date](table) {
  implicit object DateIsTimeSeries extends TimeSeries[Date]
  val ev = implicitly[TimeSeries[Date]]
}

/**
 * A DateTime Column, used to enforce restrictions on clustering order.
 * @param table The Cassandra Table to which the column belongs to.
 * @tparam Owner The Owner of the Record.
 * @tparam Record The Record type.
 */
class DateTimeColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends PrimitiveColumn[Owner, Record, DateTime](table) {
  implicit object DateTimeIsTimeSeries extends TimeSeries[DateTime]
  val ev = implicitly[TimeSeries[DateTime]]
}