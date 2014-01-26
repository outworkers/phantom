package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.CassandraTable
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.column.JsonColumn
import com.newzly.phantom.keys.PrimaryKey

case class MyTestRow(
  key: String,
  optionA: Option[Int],
  classS: SimpleStringClass
)

object MyTestRow extends ModelSampler[MyTestRow] {
  def sample: MyTestRow = MyTestRow(
    Sampler.getAUniqueString,
    Some(Sampler.getARandomInteger()),
    SimpleStringClass.sample
  )
}

sealed class MyTest extends CassandraTable[MyTest, MyTestRow] {
  def fromRow(r: Row): MyTestRow = {
    MyTestRow(key(r), optionA(r), classS(r))
  }

  object key extends StringColumn(this) with PrimaryKey[MyTest, MyTestRow]

  object stringlist extends ListColumn[MyTest, MyTestRow, String](this)

  object optionA extends OptionalPrimitiveColumn[MyTest, MyTestRow, Int](this)

  object classS extends JsonColumn[MyTest, MyTestRow, SimpleStringClass](this)
}

object MyTest extends MyTest with TestSampler[MyTest, MyTestRow] {
  override val tableName = "mytest"
  def createSchema = {
    """|CREATE TABLE MyTestInsert(
      |key text PRIMARY KEY,
      |stringlist list<text>,
      |optionA int,
      |classS text
      );
    """.stripMargin
  }
}


