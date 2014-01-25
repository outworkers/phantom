package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{Sampler, SimpleMapOfStringsClass, TestSampler}


case class TestRow(
  key: String,
  list: Seq[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String]
)


sealed class TestTable extends CassandraTable[TestTable, TestRow] {

  object key extends PrimitiveColumn[String]

  object list extends SeqColumn[String]

  object setText extends SetColumn[String]

  object mapTextToText extends MapColumn[String, String]

  object setInt extends SetColumn[Int]

  object mapIntToText extends MapColumn[Int, String]

  def fromRow(r: Row): TestRow = {
    TestRow(
      key(r),
      list(r),
      setText(r),
      mapTextToText(r),
      setInt(r).toSet,
      mapIntToText(r)
    )
  }

  val _key = key
}

object TestTable extends TestTable with TestSampler[TestRow] {
  def sample: TestRow = {

    /**
     * Generates a random unique row for a TestRow cassandra table.
     * @return A unique Test Row with nested JSON structures..
     */
    def sample: TestRow = {
      TestRow(
        Sampler.getAUniqueString,
        Some(Sampler.getARandomInteger()),
        SimpleMapOfStringsClass(Map(Sampler.getAUniqueString -> Sampler.getARandomInteger())),
        Some(SimpleMapOfStringsClass(Map(Sampler.getAUniqueString -> Sampler.getARandomInteger()))),
        Map(Sampler.getAUniqueString -> SimpleMapOfStringsClass(Map(Sampler.getAUniqueString -> Sampler.getARandomInteger())))
      )
    }
  }

  def createSchema: String = {
    ""
  }
}

