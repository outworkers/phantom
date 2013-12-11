package com.newzly.cassandra.phantom.field

import java.util.UUID
import com.newzly.cassandra.phantom.{ CassandraTable, PrimitiveColumn }

trait UUIDPk extends PrimitiveColumn[UUIDPk] {
  this: CassandraTable[_, _] =>
  override lazy val name = "id"
}