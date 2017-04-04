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

import com.datastax.driver.core.{
  ColumnDefinitions,
  ExecutionInfo,
  ProtocolVersion,
  TupleValue,
  Row => DatastaxRow,
  ResultSet => DatastaxResultSet
}
import com.google.common.util.concurrent.ListenableFuture

class Row(val inner: DatastaxRow, val version: ProtocolVersion) {
  def getBytesUnsafe(name: String): ByteBuffer = inner.getBytesUnsafe(name)
  def getBytesUnsafe(index: Int): ByteBuffer = inner.getBytesUnsafe(index)


  def getColumnDefinitions: ColumnDefinitions = inner.getColumnDefinitions

  def getTupleValue(name: String): TupleValue = inner.getTupleValue(name)

  def isNull(name: String): Boolean = inner.isNull(name)
}

case class ResultSet(
  inner: DatastaxResultSet,
  version: ProtocolVersion
) extends DatastaxResultSet {

  def value(): Row = new Row(one(), version)

  override def one(): DatastaxRow = inner.one()

  override def wasApplied(): Boolean = inner.wasApplied()

  override def getColumnDefinitions: ColumnDefinitions = inner.getColumnDefinitions

  override def getExecutionInfo: ExecutionInfo = inner.getExecutionInfo

  override def fetchMoreResults(): ListenableFuture[DatastaxResultSet] = inner.fetchMoreResults()

  override def isExhausted: Boolean = inner.isExhausted

  override def all(): util.List[DatastaxRow] = inner.all()

  def allRows(): List[Row] = inner.all().asScala.map(r => new Row(r, version)).toList

  override def isFullyFetched: Boolean = inner.isFullyFetched

  override def getAllExecutionInfo: util.List[ExecutionInfo] = inner.getAllExecutionInfo

  override def iterator(): util.Iterator[DatastaxRow] = inner.iterator()

  def iterate(): Iterator[Row] = inner.iterator().asScala.map(r => new Row(r, version))

  override def getAvailableWithoutFetching: Int = inner.getAvailableWithoutFetching
}