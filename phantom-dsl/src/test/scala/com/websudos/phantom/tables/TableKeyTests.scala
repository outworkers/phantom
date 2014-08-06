package com.websudos.phantom.tables

import java.util.UUID

import com.datastax.driver.core.Row
import com.websudos.phantom.Implicits._
import com.websudos.phantom.PhantomCassandraConnector


case class StubRecord(name: String, id: UUID)
sealed class TableWithSingleKey extends CassandraTable[TableWithSingleKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithSingleKey extends TableWithSingleKey with PhantomCassandraConnector

class TableWithCompoundKey extends CassandraTable[TableWithCompoundKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompoundKey extends TableWithCompoundKey with PhantomCassandraConnector


sealed class TableWithCompositeKey extends CassandraTable[TableWithCompositeKey, StubRecord] {

  object id extends UUIDColumn(this) with PartitionKey[UUID]
  object second_part extends UUIDColumn(this) with PartitionKey[UUID]
  object second extends UUIDColumn(this) with PrimaryKey[UUID]
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithCompositeKey extends TableWithCompositeKey with PhantomCassandraConnector

sealed class TableWithNoKey extends CassandraTable[TableWithNoKey, StubRecord] {

  object id extends UUIDColumn(this)
  object name extends StringColumn(this)

  def fromRow(r: Row): StubRecord = {
    StubRecord(name(r), id(r))
  }
}

object TableWithNoKey extends TableWithNoKey with PhantomCassandraConnector
