package com.newzly.phantom.query

import java.nio.ByteBuffer
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.policies.RetryPolicy

private[phantom] trait CQLQuery[Q] extends ExecutableStatement {

  self: Q =>

  def tracing_=(flag: Boolean): Q = {
    if (flag)
      qb.enableTracing()
    else
      qb.disableTracing()
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
