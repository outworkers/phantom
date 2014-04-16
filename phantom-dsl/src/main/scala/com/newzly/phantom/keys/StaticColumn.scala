package com.newzly.phantom.keys

import com.newzly.phantom.column.AbstractColumn

trait StaticColumn[ValueType] extends Key[ValueType, PartitionKey[ValueType]] {
  self: AbstractColumn[ValueType] =>
    override val isStaticColumn = true
}
