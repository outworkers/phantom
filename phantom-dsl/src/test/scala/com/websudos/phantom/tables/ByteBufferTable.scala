package com.websudos.phantom.tables

import java.nio.ByteBuffer
import java.util.UUID

import com.websudos.phantom.builder.query.InsertQuery
import com.websudos.phantom.dsl._

import scala.concurrent.Future

case class BufferRecord(id: UUID, buffer: ByteBuffer)

sealed class ByteBufferTable extends CassandraTable[ConcreteByteBufferTable, BufferRecord] {
  object id extends UUIDColumn(this) with PartitionKey[UUID]

  object buffer extends BlobColumn(this)

  def fromRow(row: Row): BufferRecord = {
    BufferRecord(
      id(row),
      buffer(row)
    )
  }
}

abstract class ConcreteByteBufferTable extends ByteBufferTable with RootConnector {

  def store(record: BufferRecord): InsertQuery.Default[ConcreteByteBufferTable, BufferRecord] = {
    insert.value(_.id, record.id)
      .value(_.buffer, record.buffer)
  }

  def getById(id: UUID): Future[Option[BufferRecord]] = {
    select.where(_.id eqs id).one()
  }
}
