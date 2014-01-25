package com.newzly.phantom.tables

import com.newzly.phantom.{JsonColumn, OptionalPrimitiveColumn, PrimitiveColumn, CassandraTable}
import com.datastax.driver.core.Row
import com.newzly.phantom.tables.MyTestRow
import com.newzly.phantom.tables.MyTestRow

case class MyTestRow(key: String, optionA: Option[Int], classS: SimpleStringClass)

class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), classS(r))
  }

  object key extends PrimitiveColumn[String]

  object optionA extends OptionalPrimitiveColumn[Int]

  object classS extends JsonColumn[SimpleStringClass]

  val _key = key
}
