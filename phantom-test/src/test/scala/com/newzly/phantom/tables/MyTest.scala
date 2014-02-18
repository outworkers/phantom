package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.keys.{PartitionKey, PrimaryKey}

case class MyTestRow(
  key: String,
  optionA: Option[Int],
  stringlist: List[String]
)

object MyTestRow extends ModelSampler[MyTestRow] {
  def sample: MyTestRow = MyTestRow(
    Sampler.getAUniqueString,
    Some(Sampler.getARandomInteger()),
    List.range(0, 20).map(x => Sampler.getAUniqueString)
  )
}

sealed class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), stringlist(r))
  }

  object key extends StringColumn(this) with PartitionKey[String]

  object stringlist extends ListColumn[MyTest, MyTestRow, String](this)

  object optionA extends OptionalIntColumn(this)

}

object MyTest extends MyTest with TestSampler[MyTest, MyTestRow] {
  override val tableName = "mytest"

}


