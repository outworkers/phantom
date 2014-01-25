package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.{
  CassandraTable,
  JsonColumn,
  OptionalPrimitiveColumn,
  PrimitiveColumn
}
import com.newzly.phantom.helper.{ Sampler, TestSampler }

case class MyTestRow(
  key: String,
  optionA: Option[Int],
  classS: SimpleStringClass
)

sealed class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), classS(r))
  }

  object key extends PrimitiveColumn[String]

  object optionA extends OptionalPrimitiveColumn[Int]

  object classS extends JsonColumn[SimpleStringClass]

  val _key = key
}

object MyTest extends MyTest with TestSampler[MyTestRow] {
  def sample: MyTestRow = {
    MyTestRow(
      Sampler.getAUniqueString,
      Some(Sampler.getARandomInteger())
    )
  }
}


