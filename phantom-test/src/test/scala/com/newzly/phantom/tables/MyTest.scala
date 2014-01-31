package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.keys.PrimaryKey

case class MyTestRow(
  key: String,
  optionA: Option[Int]
)

object MyTestRow extends ModelSampler[MyTestRow] {
  def sample: MyTestRow = MyTestRow(
    Sampler.getAUniqueString,
    Some(Sampler.getARandomInteger())
  )
}

sealed class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r))
  }

  def meta = MyTest

  object key extends StringColumn(this) with PrimaryKey

  object stringlist extends ListColumn[MyTest, MyTestRow, String](this)

  object optionA extends OptionalPrimitiveColumn[MyTest, MyTestRow, Int](this)

}

object MyTest extends MyTest with TestSampler[MyTest, MyTestRow] {
  override val tableName = "mytest"

}


