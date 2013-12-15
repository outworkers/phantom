package com.newzly.cassandra.phantom.field

import java.util.UUID
import com.newzly.cassandra.phantom.{ CassandraTable, PrimitiveColumn }

trait UUIDPk[Owner <: CassandraTable[Owner, _]] {
  this: CassandraTable[Owner, _] =>

  object id extends PrimitiveColumn[UUID]

  val _key = id;
}

trait LongOrderKey[Owner <: CassandraTable[Owner, _]] {
  this: CassandraTable[Owner, _] with UUIDPk[Owner] =>
  object order_id extends PrimitiveColumn[Long]
}