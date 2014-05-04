package com.newzly.phantom.column

trait CollectionValueDefinition[RR] {

  def valueCls: Class[_]
  def valueToCType(v: RR): AnyRef
  def valueFromCType(c: AnyRef): RR
}
