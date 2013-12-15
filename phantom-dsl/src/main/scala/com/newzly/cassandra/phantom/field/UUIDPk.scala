package com.newzly.cassandra.phantom.field

import java.util.UUID
import com.newzly.cassandra.phantom.{ CassandraTable, PrimitiveColumn }

trait UUIDPk[Owner <: CassandraTable[Owner, Record], Record] {
  this: CassandraTable[Owner, Record] =>

  object id extends PrimitiveColumn[UUID] {}

  val _key = id;
}

trait TimeUUIDPk[Owner <: CassandraTable[Owner, Record], Record] {
  this: CassandraTable[Owner, Record] =>

  object id extends PrimitiveColumn[UUID] {

  }
  val _key = id;
}

trait LongOrderKey[Owner <: CassandraTable[Owner, _], Record] {
  this: CassandraTable[Owner, Record] with UUIDPk[Owner, Record] =>
  object order_id extends PrimitiveColumn[Long]
}