package com.websudos.phantom.udt

import com.websudos.phantom.Implicits._


case class TestRecord(id: UUID, name: String)

sealed class TestFields extends CassandraTable[TestFields, TestRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  object udt extends UDTColumn(this) {

  }

  def fromRow(row: Row): TestRecord = {
    TestRecord(
      id(row),
      name(row)
    )
  }
}
