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

import com.datastax.driver.core.ConsistencyLevel

class QueryOptions(
  val consistencyLevel: Option[ConsistencyLevel],
  val serialConsistencyLevel: Option[ConsistencyLevel],
  val pagingState: Option[String] = None,
  val enableTracing: Option[Boolean] = None,
  val fetchSize: Option[Int] = None
) {

  def options: com.datastax.driver.core.QueryOptions = {
    val opt = new com.datastax.driver.core.QueryOptions()

    consistencyLevel map opt.setConsistencyLevel
    serialConsistencyLevel map opt.setSerialConsistencyLevel
    fetchSize map opt.setFetchSize

    opt
  }

  def consistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    new QueryOptions(
      Some(level),
      serialConsistencyLevel,
      pagingState,
      enableTracing,
      fetchSize
    )
  }

  def serialConsistencyLevel_=(level: ConsistencyLevel): QueryOptions = {
    new QueryOptions(
      consistencyLevel,
      Some(level),
      pagingState,
      enableTracing,
      fetchSize
    )
  }

  def enableTracing_=(flag: Boolean): QueryOptions = {
    new QueryOptions(
      consistencyLevel,
      serialConsistencyLevel,
      pagingState,
      Some(flag),
      fetchSize
    )
  }

  def fetchSize_=(size: Int): QueryOptions = {
    new QueryOptions(
      consistencyLevel,
      serialConsistencyLevel,
      pagingState,
      enableTracing,
      Some(size)
    )
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