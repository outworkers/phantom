package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom.helper.{ ModelSampler, Sampler, TestSampler }
import com.newzly.phantom.Implicits._
import com.newzly.phantom.keys.PrimaryKey

case class TestRow(
  key: String,
  list: Seq[String],
  setText: Set[String],
  mapTextToText: Map[String, String],
  setInt: Set[Int],
  mapIntToText: Map[Int, String]
)

object TestRow extends ModelSampler[TestRow] {
  def sample: TestRow = TestRow(
    Sampler.getAUniqueString,
    List.range(0, 50).map(_.toString).toSeq,
    List.range(0, 50).map(_.toString).toSet,
    List.range(0, 50).map(x => {Sampler.getAUniqueString -> Sampler.getAUniqueString}).toMap,
    List.range(0, 50).toSet,
    List.range(0, 50).map(x => {
      x -> Sampler.getAUniqueString
    }).toMap
  )
}

sealed class TestTable extends CassandraTable[TestTable, TestRow] {

  def meta = TestTable

  object key extends StringColumn(this) with PrimaryKey

  object list extends SeqColumn[TestTable, TestRow, String](this)

  object setText extends SetColumn[TestTable, TestRow, String](this)

  object mapTextToText extends MapColumn[TestTable, TestRow, String, String](this)

  object setInt extends SetColumn[TestTable, TestRow, Int](this)

  object mapIntToText extends MapColumn[TestTable, TestRow, Int, String](this)

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
}

object TestTable extends TestTable with TestSampler[TestTable, TestRow] {
  override val tableName = "TestTable"
}

