package com.newzly.phantom.query

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.querybuilder.BuiltStatement
import com.datastax.driver.core.policies.RetryPolicy
import java.nio.ByteBuffer

private[query] abstract class SharedQueryMethods[Q, T <: BuiltStatement](builder: T) {
  self: Q =>

  def setConsistencyLevel(level: ConsistencyLevel): Q = {
    builder.setConsistencyLevel(level)
    this
  }

  def getRetryPolicy(): RetryPolicy = {
    builder.getRetryPolicy
  }

  def getRoutingKey(): ByteBuffer = {
    builder.getRoutingKey
  }
}
