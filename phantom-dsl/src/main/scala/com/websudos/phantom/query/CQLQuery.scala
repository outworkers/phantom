/*
 * Copyright 2013-2015 Websudos, Limited.
 *
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * - Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 *
 * - Redistributions in binary form must reproduce the above copyright
 * notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * - Explicit consent must be obtained from the copyright owner, Websudos Limited before any redistribution is made.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
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
