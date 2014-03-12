package com.newzly.phantom.keys

import com.newzly.phantom.CassandraTable
import com.newzly.phantom.column.PrimitiveColumn

/**
 * A key used for partition tokens and token functions.
 * @tparam Owner The owner of the table.
 * @tparam Record The type of the record.
 */
trait LongOrderKey[Owner <: CassandraTable[Owner, Record], Record] {
  this: CassandraTable[Owner, Record] =>
  object order_id extends PrimitiveColumn[Owner, Record, Long](this) {
    override val isPrimary = true
  }
}
