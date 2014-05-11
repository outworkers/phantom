package com.newzly.phantom.column

import com.newzly.phantom.CassandraPrimitive

trait PrimitiveCollecitonValue[R] extends CollectionValueDefinition[R] {

  def valuePrimitive: CassandraPrimitive[R]

  override def valueCls = valuePrimitive.cls

  override def valueToCType(v: R): AnyRef = valuePrimitive.toCType(v)

  override def valueFromCType(c: AnyRef): R = valuePrimitive.fromCType(c)

}
