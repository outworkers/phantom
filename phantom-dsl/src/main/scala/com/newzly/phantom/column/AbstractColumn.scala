package com.newzly.phantom.column

import java.util.concurrent.atomic.AtomicBoolean
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraWrites
trait Keys {
  val isPrimary: Boolean = false
  val isSecondaryKey: Boolean = false
  val isPartitionKey: Boolean = false
}
trait AbstractColumn[T] extends CassandraWrites[T] with Keys {

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")

  def optional(r: Row): Option[T]
}
