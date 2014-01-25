package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }

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

  object key extends PrimitiveColumn[String]

  object list extends ListColumn[String]

  object optionA extends OptionalPrimitiveColumn[Int]

  object classS extends JsonColumn[SimpleStringClass]

  val _key = key
}

object MyTest extends MyTest with TestSampler[MyTestRow] {
  override val tableName = "mytest"
  def createSchema = {
    """|CREATE TABLE MyTestInsert(
      |key text PRIMARY KEY,
      |list<string>,
      |optionA int,
      |classS text,
        );
    """.stripMargin
  }
}


