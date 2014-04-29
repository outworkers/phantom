package com.newzly.phantom;

trait CassandraPrimitive[T] extends CassandraWrites[T] {

  def cls: Class[_]
  def toCType(v: T): AnyRef = v.asInstanceOf[AnyRef]
  def fromCType(c: AnyRef): T = c.asInstanceOf[T]
  def fromRow(row: Row, name: String): Option[T]
}
