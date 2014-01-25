package com.newzly.phantom.column

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable

class EnumColumn[Owner <: CassandraTable[Owner, Record], Record, EnumType <: Enumeration](table: CassandraTable[Owner, Record], enum: EnumType) extends Column[Owner, Record, EnumType#Value](table) {

  def toCType(v: EnumType#Value): AnyRef = v.toString
  def cassandraType: String = "???"
  def optional(r: Row): Option[EnumType#Value] =
    Option(r.getString(name)).flatMap(s => enum.values.find(_.toString == s))
}
