package com.newzly.phantom.column

import com.newzly.phantom.CassandraTable
import java.util.UUID

class TimeUUIDColumn[Owner <: CassandraTable[Owner, Record], Record](table: CassandraTable[Owner, Record]) extends PrimitiveColumn[Owner, Record, UUID](table) {
  override val cassandraType = "timeuuid"

}
