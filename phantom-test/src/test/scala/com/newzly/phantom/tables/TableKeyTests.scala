package com.newzly.phantom.tables

import java.util.UUID
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.Implicits._
import com.datastax.driver.core.Row


case class StubRecord(name: String, id: UUID)
sealed class TableWithSingleKey extends CassandraTable[TableWithSingleKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithSingleKey extends TableWithSingleKey

class TableWithCompoundKey extends CassandraTable[TableWithCompoundKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompoundKey extends TableWithCompoundKey



sealed class TableWithCompositeKey extends CassandraTable[TableWithCompositeKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second_part extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompositeKey extends TableWithCompositeKey

sealed class TableWithNoKey extends CassandraTable[TableWithNoKey, StubRecord] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithNoKey extends TableWithNoKey