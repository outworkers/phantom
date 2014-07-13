/*
 * Copyright 2013 websudos ltd.
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
package com.websudos.phantom.query

import java.nio.ByteBuffer
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.policies.RetryPolicy

private[phantom] trait CQLQuery[Q] extends ExecutableStatement {

  self: Q =>

  def setFetchSize(n: Int): Q = {
    qb.setFetchSize(n)
    this
  }

  def tracing_=(flag: Boolean): Q = {
    if (flag) {
      qb.enableTracing()

    } else {
      qb.disableTracing()
    }
    this
  }

  def queryString: String = {
    qb.toString
  }

  def consistencyLevel: ConsistencyLevel = qb.getConsistencyLevel

  def consistencyLevel_=(level: ConsistencyLevel): Q = {
    qb.setConsistencyLevel(level)
    this
  }

  def retryPolicy(): RetryPolicy = {
    qb.getRetryPolicy
  }

  def retryPolicy_= (policy: RetryPolicy): Q = {
    qb.setRetryPolicy(policy)
    this
  }

  def serialConsistencyLevel_=(level: ConsistencyLevel): Q = {
    qb.setSerialConsistencyLevel(level)
    this
  }

  def serialConsistencyLevel: ConsistencyLevel = qb.getSerialConsistencyLevel

  def forceNoValues_=(flag: Boolean): Q = {
    qb.setForceNoValues(flag)
    this
  }

  def routingKey(): ByteBuffer = {
    qb.getRoutingKey
  }
}
