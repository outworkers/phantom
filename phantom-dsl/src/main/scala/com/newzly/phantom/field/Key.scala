package com.newzly.phantom.field

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.Column

/**
 * A trait mixable into Column definitions to allow storing them as keys.
 * @tparam Owner The owner of the record.
 * @tparam Record The case class record to store.
 * @tparam ValueType The type of the value to store as a key.
 */
trait Key[Owner <: CassandraTable[Owner, Record], Record, ValueType] {
  self: Column[Owner, Record, ValueType] =>

  this.getTable.addKey(this)
}
