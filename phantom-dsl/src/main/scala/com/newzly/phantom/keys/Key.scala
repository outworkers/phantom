package com.newzly.phantom.keys

import java.util.Date
import org.joda.time.DateTime
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.Column
import com.newzly.phantom.Implicits._

/**
 * A trait mixable into Column definitions to allow storing them as keys.
 * @tparam Owner The owner of the record.
 * @tparam Record The case class record to store.
 * @tparam ValueType The type of the value to store as a key.
 */
trait Key[Owner <: CassandraTable[Owner, Record], Record, ValueType] {
  self: Column[Owner, Record, ValueType] =>

  _isKey.set(true)

  this.getTable.addKey(this)
}



/**
 * A trait mixable into a Column to allow clustering order.
 * @tparam Owner The owner of the record.
 * @tparam Record The case class record to store.
 */
trait ClusteringOrder[Owner <: CassandraTable[Owner, Record], Record, ValueType] {

  self: TimeColumn[Owner, Record, _] =>

  _isKey.set(true)

  this.getTable.addKey(this)
}
