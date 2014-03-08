package com.newzly.phantom.column

import java.util.concurrent.atomic.AtomicBoolean
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraWrites
import com.newzly.phantom.query.QueryCondition
import com.datastax.driver.core.querybuilder.QueryBuilder

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends CassandraWrites[T] {

  val isPrimary: Boolean = false
  val isSecondaryKey: Boolean = false
  val isPartitionKey: Boolean = false

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")


  def eqs (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.eq(this.name, this.toCType(value)))
  }

  def lt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.lt(this.name, this.toCType(value)))
  }

  def gt (value: T): QueryCondition = {
    QueryCondition(QueryBuilder.gt(this.name, this.toCType(value)))
  }
}
