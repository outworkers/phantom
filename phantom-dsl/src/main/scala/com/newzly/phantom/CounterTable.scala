package com.newzly.phantom

trait CounterTable[Owner <: CassandraTable[Owner, Record], Record] {
  self : CassandraTable[Owner, Record] =>

  override def schema(): String = {
    self.schema() + "WITH default_validation_class=CounterColumnType"
  }
}
