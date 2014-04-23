package com.newzly.phantom.query

import java.nio.ByteBuffer
import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.policies.RetryPolicy

private[query] abstract class SharedQueryMethods[Q, T <: BuiltStatement](builder: T) {
  self: Q =>

  def setConsistencyLevel(level: ConsistencyLevel): Q = {
    builder.setConsistencyLevel(level)
    this
  }

  def setRetryPolicy(policy: RetryPolicy): Q = {
    builder.setRetryPolicy(policy)
    this
  }

  def setSerialConsistencyLevel(level: ConsistencyLevel): Q = {
    builder.setSerialConsistencyLevel(level)
    this
  }

  def getRetryPolicy(): RetryPolicy = {
    builder.getRetryPolicy
  }

  def getRoutingKey(): ByteBuffer = {
    builder.getRoutingKey
  }
}
