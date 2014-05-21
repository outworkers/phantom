package com.newzly.phantom.query

import java.nio.ByteBuffer
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.policies.RetryPolicy
import com.datastax.driver.core.querybuilder.BuiltStatement

private[query] abstract class SharedQueryMethods[Q, T <: BuiltStatement](builder: T) extends ExecutableStatement {
  self: Q =>

  def consistencyLevel: ConsistencyLevel = builder.getConsistencyLevel

  def consistencyLevel_=(level: ConsistencyLevel): Q = {
    builder.setConsistencyLevel(level)
    this
  }

  def retryPolicy(): RetryPolicy = {
    builder.getRetryPolicy
  }

  def retryPolicy_= (policy: RetryPolicy): Q = {
    builder.setRetryPolicy(policy)
    this
  }

  def setSerialConsistencyLevel(level: ConsistencyLevel): Q = {
    builder.setSerialConsistencyLevel(level)
    this
  }

  def routingKey(): ByteBuffer = {
    builder.getRoutingKey
  }
}
