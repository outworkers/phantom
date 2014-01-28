package com.newzly.phantom.column

import com.newzly.phantom.CassandraWrites
import com.datastax.driver.core.Row

trait AbstractColumn[T] extends CassandraWrites[T] {

  type ValueType

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")

  def apply(r: Row): ValueType

  def optional(r: Row): Option[T]
}
