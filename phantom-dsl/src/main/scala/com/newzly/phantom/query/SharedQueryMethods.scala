package com.newzly.phantom.query

import com.datastax.driver.core.ConsistencyLevel
import com.datastax.driver.core.querybuilder.BuiltStatement

private[query] class SharedQueryMethods[Q, T <: BuiltStatement](builder: T) {
  self: Q =>

  def useConsistencyLevel(level: ConsistencyLevel): Q = {
    builder.setConsistencyLevel(level)
    this
  }
}
