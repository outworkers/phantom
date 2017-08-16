/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom

import java.nio.ByteBuffer
import java.util

import scala.collection.JavaConverters._
import com.datastax.driver.core.{ColumnDefinitions, ExecutionInfo, GettableByIndexData, ProtocolVersion, ResultSet => DatastaxResultSet, Row => DatastaxRow}
import com.google.common.util.concurrent.ListenableFuture

import scala.util.Try

case class ResultSet(
  inner: DatastaxResultSet,
  version: ProtocolVersion
) extends DatastaxResultSet {

  def value(): Option[Row] = if (inner.getAvailableWithoutFetching == 0){
    None
  } else {
    Some(new Row(one(), version))
  }

  override def one(): DatastaxRow = inner.one()

  override def wasApplied(): Boolean = inner.wasApplied()

  override def getColumnDefinitions: ColumnDefinitions = inner.getColumnDefinitions

  override def getExecutionInfo: ExecutionInfo = inner.getExecutionInfo

  override def fetchMoreResults: ListenableFuture[DatastaxResultSet] = inner.fetchMoreResults()

  override def isExhausted: Boolean = inner.isExhausted

  override def all(): util.List[DatastaxRow] = inner.all()

  def allRows(): List[Row] = inner.all().asScala.map(r => new Row(r, version)).toList

  override def isFullyFetched: Boolean = inner.isFullyFetched

  override def getAllExecutionInfo: util.List[ExecutionInfo] = inner.getAllExecutionInfo

  override def iterator: util.Iterator[DatastaxRow] = inner.iterator()

  def iterate(): Iterator[Row] = inner.iterator().asScala.map(r => new Row(r, version))

  override def getAvailableWithoutFetching: Int = inner.getAvailableWithoutFetching
}


class Row(val inner: DatastaxRow, val version: ProtocolVersion) {
  def getBytesUnsafe(name: String): ByteBuffer = inner.getBytesUnsafe(name)

  def getBytesUnsafe(index: Int): ByteBuffer = inner.getBytesUnsafe(index)

  def getColumnDefinitions: ColumnDefinitions = inner.getColumnDefinitions

  def isNull(name: String): Boolean = Try(inner.isNull(name)).getOrElse(true)

  def isNull(index: Int): Boolean = Try(inner.isNull(index)).getOrElse(true)
}


trait BytesExtractor[T] {
  def getBytesUnsafe(source: T, index: Int): ByteBuffer

  def getBytesUnsafe(source: T, name: String): ByteBuffer

  def isNull(source: T, name: String): Boolean

  def isNull(source: T, index: Int): Boolean

}

object BytesExtractor {
  implicit object RowExtractor extends BytesExtractor[Row] {
    override def getBytesUnsafe(source: Row, index: Int): ByteBuffer = source.getBytesUnsafe(index)

    override def getBytesUnsafe(source: Row, name: String): ByteBuffer = source.getBytesUnsafe(name)

    override def isNull(source: Row, name: String): Boolean = source.isNull(name)

    override def isNull(source: Row, index: Int): Boolean = source.isNull(index)
  }

  implicit object GettableByIndexExtractor extends BytesExtractor[GettableByIndexData] {
    override def getBytesUnsafe(source: GettableByIndexData, index: Int): ByteBuffer = source.getBytesUnsafe(index)

    override def getBytesUnsafe(source: GettableByIndexData, name: String): ByteBuffer = ByteBuffer.allocate(0)

    override def isNull(source: GettableByIndexData, name: String): Boolean = true

    override def isNull(source: GettableByIndexData, index: Int): Boolean = source.isNull(index)
  }
}