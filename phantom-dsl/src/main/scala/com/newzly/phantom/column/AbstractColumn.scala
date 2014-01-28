package com.newzly.phantom.column

import java.util.concurrent.atomic.AtomicBoolean
import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraWrites

trait AbstractColumn[T] extends CassandraWrites[T] {

  private[this] lazy val _isKey = new AtomicBoolean(false)
  private[this] lazy val _isPrimaryKey = new AtomicBoolean(false)

  protected[phantom] def setAsKey(): Unit = _isKey.compareAndSet(false, true)
  protected[phantom] def setAsPrimaryKey(): Unit = _isPrimaryKey.compareAndSet(false, true)

  def isPrimary: Boolean = _isPrimaryKey.get()
  def isKey: Boolean = _isKey.get()

  lazy val name: String = getClass.getSimpleName.replaceAll("\\$+", "").replaceAll("(anonfun\\d+.+\\d+)|", "")

  def optional(r: Row): Option[T]
}
