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
package com.outworkers.phantom.builder.query

import com.datastax.driver.core.{ConsistencyLevel, PagingState, Statement}

class ConsistencyLevelModifier(level: Option[ConsistencyLevel]) extends (Statement => Statement) {
  override def apply(v1: Statement): Statement = {
    (level map v1.setConsistencyLevel).getOrElse(v1)
  }
}

class SerialConsistencyLevelModifier(level: Option[ConsistencyLevel]) extends (Statement => Statement) {
  override def apply(v1: Statement): Statement = {
    (level map v1.setSerialConsistencyLevel).getOrElse(v1)
  }
}

class PagingStateModifier(level: Option[PagingState]) extends (Statement => Statement) {
  override def apply(v1: Statement): Statement = {
    (level map v1.setPagingState).getOrElse(v1)
  }
}

class EnableTracingModifier(level: Option[Boolean]) extends (Statement => Statement) {
  override def apply(v1: Statement): Statement = {
    level match {
      case Some(true) => v1.enableTracing()
      case Some(false) => v1.disableTracing()
      case None => v1
    }
  }
}

class FetchSizeModifier(level: Option[Int]) extends (Statement => Statement) {
  override def apply(v1: Statement): Statement = {
    (level map v1.setFetchSize).getOrElse(v1)
  }
}

case class QueryOptions(
  consistencyLevel: Option[ConsistencyLevel],
  serialConsistencyLevel: Option[ConsistencyLevel],
  pagingState: Option[PagingState] = None,
  enableTracing: Option[Boolean] = None,
  fetchSize: Option[Int] = None
) {

  def apply(st: Statement): Statement = {
    val applier = List(
      new ConsistencyLevelModifier(consistencyLevel),
      new SerialConsistencyLevelModifier(serialConsistencyLevel),
      new PagingStateModifier(pagingState),
      new EnableTracingModifier(enableTracing),
      new FetchSizeModifier(fetchSize)
    ) reduce(_ andThen _)

    applier(st)
  }

  def options: com.datastax.driver.core.QueryOptions = {
    val opt = new com.datastax.driver.core.QueryOptions()

    consistencyLevel map opt.setConsistencyLevel
    serialConsistencyLevel map opt.setSerialConsistencyLevel
    fetchSize map opt.setFetchSize

    opt
  }

  def consistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    this.copy(consistencyLevel = Some(level))
  }

  def serialConsistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    this.copy(serialConsistencyLevel = Some(level))
  }

  def enableTracing_=(flag: Boolean): QueryOptions = {
    this.copy(enableTracing = Some(flag))
  }

  def fetchSize_=(size: Int): QueryOptions = {
    this.copy(fetchSize = Some(size))
  }
}

object QueryOptions {
  def empty: QueryOptions = {
    new QueryOptions(
      consistencyLevel = None,
      serialConsistencyLevel = None,
      pagingState = None,
      enableTracing = None,
      fetchSize = None
    )
  }
}