package com.newzly.phantom.column

import java.util.concurrent.atomic.AtomicBoolean
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraWrites

trait AbstractColumn[@specialized(Int, Double, Float, Long, Boolean, Short) T] extends CassandraWrites[T] {

  val isPrimary: Boolean = false
  val isSecondaryKey: Boolean = false
  val isPartitionKey: Boolean = false

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")
}
