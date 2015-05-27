package com.websudos.phantom.column

import java.util.UUID
import com.websudos.phantom.CassandraTable

class OptionalTimeUUIDColumn [Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends OptionalPrimitiveColumn[Owner, Record, UUID](table) {
  override val cassandraType = "timeuuid"
}
