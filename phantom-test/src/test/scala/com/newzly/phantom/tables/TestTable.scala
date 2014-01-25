package com.newzly.phantom.tables

import com.datastax.driver.core.Row
import com.newzly.phantom._
import com.newzly.phantom.helper.{ModelSampler, Sampler, TestSampler}

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

object TestTable extends TestTable with TestSampler[TestTable, TestRow] {

  def createSchema: String = {
    """|CREATE TABLE TestTableInsert(
       |key text PRIMARY KEY,
       |list list<text>,
       |setText set<text>,
       |mapTextToText map<text,text>,
       |setInt set<int>,
       |mapIntToText map<int,text> );
    """.stripMargin
  }
}

